package com.ssajudn.bareuang.di

import android.content.Context
import androidx.room.Room
import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.BudgetDao
import com.ssajudn.bareuang.data.local.room.DueBillDao
import com.ssajudn.bareuang.data.local.room.GoalDao
import com.ssajudn.bareuang.data.local.room.TransactionDao
import com.ssajudn.bareuang.data.local.room.WalletDao
import com.ssajudn.bareuang.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "bareuang_offline.db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16
            )
        // Never allow a release upgrade to silently delete financial data.
        // Destructive fallback is limited to debug builds for local development.
        if (BuildConfig.DEBUG) builder.fallbackToDestructiveMigration(dropAllTables = true)
        return builder.build()
    }

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideDueBillDao(db: AppDatabase): DueBillDao = db.dueBillDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideWalletDao(db: AppDatabase): WalletDao = db.walletDao()

}
