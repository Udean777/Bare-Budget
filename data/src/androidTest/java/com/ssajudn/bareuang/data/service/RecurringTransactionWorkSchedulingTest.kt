package com.ssajudn.bareuang.data.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecurringTransactionWorkSchedulingTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setMinimumLoggingLevel(android.util.Log.ERROR).build(),
        )
        workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork().result.get(5, TimeUnit.SECONDS)
    }

    @After
    fun tearDown() {
        workManager.cancelAllWork().result.get(5, TimeUnit.SECONDS)
    }

    @Test
    fun ensureScheduled_enqueuesUniquePeriodicWork() {
        RecurringTransactionWorker.ensureScheduled(context)

        val infos = workManager
            .getWorkInfosForUniqueWork(RecurringTransactionWorker.UNIQUE_PERIODIC)
            .get(5, TimeUnit.SECONDS)

        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
    }
}
