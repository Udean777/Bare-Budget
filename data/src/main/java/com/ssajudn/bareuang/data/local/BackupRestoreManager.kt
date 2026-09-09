package com.ssajudn.bareuang.data.local

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalDueBillEntity
import com.ssajudn.bareuang.data.local.room.LocalGoalEntity
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.data.local.room.LocalWalletEntity
import com.ssajudn.bareuang.domain.error.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_BACKUP_BYTES = 5 * 1024 * 1024
private const val BACKUP_FORMAT_VERSION = 2

@androidx.annotation.Keep
data class BareuangBackupData(
    val version: Int = 2,
    val appVersion: String = "1.0.0",
    val backupDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val transactions: List<LocalTransactionEntity> = emptyList(),
    val dueBills: List<LocalDueBillEntity> = emptyList(),
    val budgets: List<LocalBudgetEntity> = emptyList(),
    val categoryBudgets: List<LocalCategoryBudgetEntity> = emptyList(),
    val goals: List<LocalGoalEntity> = emptyList(),
    val wallets: List<LocalWalletEntity> = emptyList()
)

private data class BareuangBackupEnvelope(
    val formatVersion: Int,
    val payload: BareuangBackupData,
    val sha256: String
)

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) : com.ssajudn.bareuang.domain.port.BackupRestorePort {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun checksum(payload: String): String = MessageDigest.getInstance("SHA-256")
        .digest(payload.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    override suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val payload = BareuangBackupData(
            transactions = db.transactionDao().getAllTransactions(),
            dueBills = db.dueBillDao().getAllDueBills(),
            budgets = db.budgetDao().getAllBudgets(),
            categoryBudgets = db.budgetDao().getAllCategoryBudgets(),
            goals = db.goalDao().getAllGoals(),
            wallets = db.walletDao().getAllWallets()
        )
        val payloadJson = gson.toJson(payload)
        gson.toJson(BareuangBackupEnvelope(BACKUP_FORMAT_VERSION, payload, checksum(payloadJson)))
    }

    suspend fun exportBackupToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = createBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                OutputStreamWriter(stream, StandardCharsets.UTF_8).use { it.write(json) }
            } ?: return@withContext Result.failure(AppException.DataException("Cannot open file for writing"))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Backup", "export failed", e)
            Result.failure(if (e is AppException) e else AppException.DataException(cause = e))
        }
    }

    override suspend fun exportBackup(uri: String): Result<Unit> = exportBackupToUri(Uri.parse(uri))

    private fun validateDate(value: String?): Boolean =
        value == null || Regex("^\\d{4}-\\d{2}-\\d{2}([ T].*)?$").matches(value)

    private fun requireValid(condition: Boolean, message: String) {
        if (!condition) throw AppException.DataException(message)
    }

    private suspend fun validateAndNormalize(input: BareuangBackupData): BareuangBackupData {
        requireValid(input.version in 1..BACKUP_FORMAT_VERSION, "Versi backup tidak didukung")
        requireValid(
            input.transactions.size + input.dueBills.size + input.budgets.size +
                input.categoryBudgets.size + input.goals.size + input.wallets.size <= 100_000,
            "Backup berisi terlalu banyak record"
        )
        val walletIds = input.wallets.map { it.id }
        requireValid(walletIds.all { it.isNotBlank() } && walletIds.toSet().size == walletIds.size, "ID wallet duplikat atau kosong")
        val existingWallets = db.walletDao().getAllWallets()
        val knownWallets = walletIds.toSet() + existingWallets.map { it.id }.toSet()
        val txIds = input.transactions.map { it.id }
        requireValid(txIds.all { it.isNotBlank() } && txIds.toSet().size == txIds.size, "ID transaksi duplikat atau kosong")
        requireValid(
            input.transactions.all {
                it.amount > 0 && validateDate(it.date) &&
                    (it.walletId == null || it.walletId in knownWallets) &&
                    (it.toWalletId == null || it.toWalletId in knownWallets)
            },
            "Data transaksi tidak valid atau merujuk wallet yang hilang"
        )
        requireValid(input.dueBills.all { it.id.isNotBlank() && it.providerName.isNotBlank() && it.totalAmount > 0 && validateDate(it.dueDate) }, "Data tagihan tidak valid")
        requireValid(input.budgets.all { Regex("^\\d{4}-\\d{2}$").matches(it.monthYear) && it.monthlyLimit >= 0 }, "Data budget tidak valid")
        requireValid(input.categoryBudgets.all { Regex("^\\d{4}-\\d{2}$").matches(it.monthYear) && it.category.isNotBlank() && it.limitAmount >= 0 }, "Data category budget tidak valid")
        requireValid(input.goals.all { it.id.isNotBlank() && it.name.isNotBlank() && it.targetAmount > 0 && it.currentAmount >= 0 }, "Data goal tidak valid")
        return input
    }

    suspend fun importBackupFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()
                requireValid(bytes.size <= MAX_BACKUP_BYTES, "File backup terlalu besar (maks 5MB)")
                String(bytes, StandardCharsets.UTF_8)
            } ?: return@withContext Result.failure(AppException.DataException("Cannot open file for reading"))

            val root = JsonParser.parseString(raw).asJsonObject
            val payload: BareuangBackupData
            if (root.has("payload") && root.has("sha256")) {
                val envelope = gson.fromJson(root, BareuangBackupEnvelope::class.java)
                val payloadJson = gson.toJson(envelope.payload)
                requireValid(
                    envelope.formatVersion == BACKUP_FORMAT_VERSION &&
                        envelope.sha256.equals(checksum(payloadJson), ignoreCase = true),
                    "Checksum backup tidak valid"
                )
                payload = envelope.payload
            } else {
                // Legacy v1 files remain importable, but are normalized and validated.
                payload = gson.fromJson(root, BareuangBackupData::class.java)
            }

            val safe = validateAndNormalize(payload)
            var restored = 0
            // Restore semantics are an atomic merge: records with the same primary
            // key are replaced, and all writes succeed or roll back together.
            db.withTransaction {
                db.walletDao().insertWallets(safe.wallets)
                restored += safe.wallets.size
                db.transactionDao().insertTransactions(safe.transactions)
                restored += safe.transactions.size
                db.dueBillDao().insertDueBills(safe.dueBills)
                restored += safe.dueBills.size
                db.budgetDao().insertBudgets(safe.budgets)
                restored += safe.budgets.size
                db.budgetDao().insertCategoryBudgets(safe.categoryBudgets)
                restored += safe.categoryBudgets.size
                db.goalDao().insertGoals(safe.goals)
                restored += safe.goals.size
            }
            Result.success(restored)
        } catch (e: Exception) {
            android.util.Log.e("Backup", "import failed", e)
            Result.failure(if (e is AppException) e else AppException.DataException("Backup tidak valid", e))
        }
    }

    override suspend fun importBackup(uri: String): Result<Int> = importBackupFromUri(Uri.parse(uri))
}
