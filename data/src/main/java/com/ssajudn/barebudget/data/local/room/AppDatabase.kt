package com.ssajudn.barebudget.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalTransactionEntity::class,
        LocalDueBillEntity::class,
        LocalBudgetEntity::class,
        LocalGoalEntity::class,
        LocalWalletEntity::class,
        OutboxEntity::class,
        CachedTranslationEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun dueBillDao(): DueBillDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun walletDao(): WalletDao
    abstract fun outboxDao(): OutboxDao
    abstract fun cachedTranslationDao(): CachedTranslationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN toWalletId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_due_bills ADD COLUMN paidWalletId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_due_bills ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_budgets ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_goals ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_wallets ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS outbox (id TEXT NOT NULL PRIMARY KEY, ownerId TEXT NOT NULL, entityType TEXT NOT NULL, entityId TEXT NOT NULL, payloadJson TEXT NOT NULL, idempotencyKey TEXT NOT NULL, state TEXT NOT NULL, attempts INTEGER NOT NULL, createdAt INTEGER NOT NULL, nextRetryAt INTEGER)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_state ON outbox(state)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_ownerId ON outbox(ownerId)")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS cached_translations (cacheKey TEXT NOT NULL PRIMARY KEY, sourceLang TEXT NOT NULL, targetLang TEXT NOT NULL, originalText TEXT NOT NULL, translatedText TEXT NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bare_budget_offline.db"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
