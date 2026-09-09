package com.ssajudn.bareuang.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/** Documents the query-plan audit for the dashboard aggregation path. */
class TransactionQueryPlanTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun dashboardExpenseAggregationHasQueryPlan() {
        val plan = database.openHelper.writableDatabase.query(
            "EXPLAIN QUERY PLAN SELECT COALESCE(SUM(amount), 0) FROM local_transactions " +
                "WHERE date >= ? AND date < ? AND isRecurringParent = 0 " +
                "AND type = 'EXPENSE' AND category != 'BILLS'",
            arrayOf("2026-01-01", "2026-02-01"),
        )

        plan.use {
            assertFalse("SQLite must return a query plan", !it.moveToFirst())
        }
    }
}
