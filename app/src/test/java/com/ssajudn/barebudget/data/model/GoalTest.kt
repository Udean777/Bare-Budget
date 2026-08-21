package com.ssajudn.barebudget.data.model

import com.ssajudn.barebudget.domain.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Characterization test for [Goal]'s derived properties. These computed
 * values are used by the UI (progress bars, remaining-amount chips) so a
 * silent regression would be immediately visible to users.
 */
class GoalTest {

    @Test
    fun `progressPercentage is zero when target is zero`() {
        val goal = Goal(name = "X", targetAmount = 0L, currentAmount = 0L)
        assertEquals(0f, goal.progressPercentage, 0.0001f)
    }

    @Test
    fun `progressPercentage is ratio when current is below target`() {
        val goal = Goal(name = "X", targetAmount = 100_000L, currentAmount = 25_000L)
        assertEquals(0.25f, goal.progressPercentage, 0.0001f)
    }

    @Test
    fun `progressPercentage is clamped to 1f when current exceeds target`() {
        val goal = Goal(name = "X", targetAmount = 100_000L, currentAmount = 150_000L)
        assertEquals(1f, goal.progressPercentage, 0.0001f)
    }

    @Test
    fun `remainingAmount is target minus current when below target`() {
        val goal = Goal(name = "X", targetAmount = 100_000L, currentAmount = 25_000L)
        assertEquals(75_000L, goal.remainingAmount)
    }

    @Test
    fun `remainingAmount is zero when current meets or exceeds target`() {
        val goal = Goal(name = "X", targetAmount = 100_000L, currentAmount = 100_000L)
        assertEquals(0L, goal.remainingAmount)

        val overGoal = Goal(name = "X", targetAmount = 100_000L, currentAmount = 150_000L)
        assertEquals(0L, overGoal.remainingAmount)
    }
}
