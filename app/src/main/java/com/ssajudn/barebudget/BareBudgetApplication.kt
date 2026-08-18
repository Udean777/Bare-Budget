package com.ssajudn.barebudget

import android.app.Application
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.data.local.room.AppDatabase

class BareBudgetApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var sessionManager: UserSessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        sessionManager = UserSessionManager(this).apply {
            initSession()
        }
    }

    companion object {
        lateinit var instance: BareBudgetApplication
            private set
    }
}
