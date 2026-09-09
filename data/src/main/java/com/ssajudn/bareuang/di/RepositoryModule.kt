package com.ssajudn.bareuang.di

import com.ssajudn.bareuang.data.repository.AnalyticsRepositoryImpl
import com.ssajudn.bareuang.data.repository.BudgetRepositoryImpl
import com.ssajudn.bareuang.data.repository.DueBillRepositoryImpl
import com.ssajudn.bareuang.data.repository.GoalRepositoryImpl
import com.ssajudn.bareuang.data.repository.TransactionRepositoryImpl
import com.ssajudn.bareuang.data.repository.WalletRepositoryImpl
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.repository.GoalRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.time.Clock
import java.time.ZoneId
import com.ssajudn.bareuang.data.local.BackupRestoreManager
import com.ssajudn.bareuang.data.local.CurrencyPreferences
import com.ssajudn.bareuang.data.local.DailyPacingPreferences
import com.ssajudn.bareuang.data.local.ImportPreferences
import com.ssajudn.bareuang.data.local.LocalDataResetter
import com.ssajudn.bareuang.data.local.OcrConsentPreferences
import com.ssajudn.bareuang.data.local.OnboardingStatePreferences
import com.ssajudn.bareuang.data.local.ThemePreferences
import com.ssajudn.bareuang.data.local.TourPreferences
import com.ssajudn.bareuang.data.local.WidgetPreferences
import com.ssajudn.bareuang.data.notification.BillReminderPrefs
import com.ssajudn.bareuang.data.notification.BillReminderScheduler
import com.ssajudn.bareuang.data.service.CsvMutasiParser
import com.ssajudn.bareuang.data.service.ReceiptAiService
import com.ssajudn.bareuang.domain.port.BackupRestorePort
import com.ssajudn.bareuang.domain.port.BillReminderPreferencesPort
import com.ssajudn.bareuang.domain.port.BillReminderSchedulerPort
import com.ssajudn.bareuang.domain.port.CsvParserPort
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import com.ssajudn.bareuang.domain.port.ImportPreferencesPort
import com.ssajudn.bareuang.domain.port.LocalDataResetPort
import com.ssajudn.bareuang.domain.port.OcrConsentPort
import com.ssajudn.bareuang.domain.port.OnboardingStatePort
import com.ssajudn.bareuang.domain.port.ReceiptAiPort
import com.ssajudn.bareuang.domain.port.ThemePreferencesPort
import com.ssajudn.bareuang.domain.port.TourPreferencesPort
import com.ssajudn.bareuang.domain.port.WidgetPreferencesPort

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    companion object {
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.system(ZoneId.of("Asia/Jakarta"))
    }

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

    @Binds abstract fun bindThemePreferences(impl: ThemePreferences): ThemePreferencesPort
    @Binds abstract fun bindCurrencyPreferences(impl: CurrencyPreferences): CurrencyPreferencesPort
    @Binds abstract fun bindWidgetPreferences(impl: WidgetPreferences): WidgetPreferencesPort
    @Binds abstract fun bindTourPreferences(impl: TourPreferences): TourPreferencesPort
    @Binds abstract fun bindImportPreferences(impl: ImportPreferences): ImportPreferencesPort
    @Binds abstract fun bindOcrConsent(impl: OcrConsentPreferences): OcrConsentPort
    @Binds abstract fun bindOnboardingState(impl: OnboardingStatePreferences): OnboardingStatePort
    @Binds abstract fun bindBackup(impl: BackupRestoreManager): BackupRestorePort
    @Binds abstract fun bindResetter(impl: LocalDataResetter): LocalDataResetPort
    @Binds abstract fun bindCsvParser(impl: CsvMutasiParser): CsvParserPort
    @Binds abstract fun bindReceiptAi(impl: ReceiptAiService): ReceiptAiPort
    @Binds abstract fun bindReminderScheduler(impl: BillReminderScheduler): BillReminderSchedulerPort
    @Binds abstract fun bindReminderPrefs(impl: BillReminderPrefs): BillReminderPreferencesPort
    @Binds abstract fun bindDailyPacingPrefs(impl: DailyPacingPreferences): DailyPacingPreferencesPort

}
