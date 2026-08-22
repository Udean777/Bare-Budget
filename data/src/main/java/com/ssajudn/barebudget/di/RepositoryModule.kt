package com.ssajudn.barebudget.di

import com.ssajudn.barebudget.data.repository.AnalyticsRepositoryImpl
import com.ssajudn.barebudget.data.repository.BudgetRepositoryImpl
import com.ssajudn.barebudget.data.repository.DueBillRepositoryImpl
import com.ssajudn.barebudget.data.repository.GoalRepositoryImpl
import com.ssajudn.barebudget.data.repository.MigrationRepositoryImpl
import com.ssajudn.barebudget.data.repository.TransactionRepositoryImpl
import com.ssajudn.barebudget.data.repository.WalletRepositoryImpl
import com.ssajudn.barebudget.domain.repository.AnalyticsRepository
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.repository.DueBillRepository
import com.ssajudn.barebudget.domain.repository.GoalRepository
import com.ssajudn.barebudget.domain.repository.MigrationRepository
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindDueBillRepository(impl: DueBillRepositoryImpl): DueBillRepository

    @Binds
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds
    abstract fun bindMigrationRepository(impl: MigrationRepositoryImpl): MigrationRepository
}
