package com.ssajudn.barebudget.data.local

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssajudn.barebudget.data.local.room.*
import com.ssajudn.barebudget.domain.error.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class BareBudgetBackupData(
    val version: Int = 1,
    val appVersion: String = "1.0.0",
    val backupDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val transactions: List<LocalTransactionEntity> = emptyList(),
    val dueBills: List<LocalDueBillEntity> = emptyList(),
    val budgets: List<LocalBudgetEntity> = emptyList(),
    val goals: List<LocalGoalEntity> = emptyList(),
    val wallets: List<LocalWalletEntity> = emptyList()
)

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val backup = BareBudgetBackupData(
            version = 1,
            appVersion = "1.0.0",
            backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            transactions = db.transactionDao().getAllTransactions(),
            dueBills = db.dueBillDao().getAllDueBills(),
            budgets = db.budgetDao().getAllBudgets(),
            goals = db.goalDao().getAllGoals(),
            wallets = db.walletDao().getAllWallets()
        )
        gson.toJson(backup)
    }

    suspend fun exportBackupToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = createBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(AppException.DataException("Cannot open file for writing"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(if (e is AppException) e else AppException.DataException(e.message, e))
        }
    }

    suspend fun importBackupFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = java.lang.StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            } ?: return@withContext Result.failure(AppException.DataException("Cannot open file for reading"))

            val backup = gson.fromJson(stringBuilder.toString(), BareBudgetBackupData::class.java)
                ?: return@withContext Result.failure(AppException.DataException("Invalid backup format"))

            var totalRestored = 0

            if (backup.wallets.isNotEmpty()) {
                db.walletDao().insertWallets(backup.wallets)
                totalRestored += backup.wallets.size
            }
            if (backup.transactions.isNotEmpty()) {
                db.transactionDao().insertTransactions(backup.transactions)
                totalRestored += backup.transactions.size
            }
            if (backup.dueBills.isNotEmpty()) {
                db.dueBillDao().insertDueBills(backup.dueBills)
                totalRestored += backup.dueBills.size
            }
            if (backup.budgets.isNotEmpty()) {
                db.budgetDao().insertBudgets(backup.budgets)
                totalRestored += backup.budgets.size
            }
            if (backup.goals.isNotEmpty()) {
                db.goalDao().insertGoals(backup.goals)
                totalRestored += backup.goals.size
            }

            Result.success(totalRestored)
        } catch (e: Exception) {
            Result.failure(if (e is AppException) e else AppException.UnknownError(e.message, e))
        }
    }
}
