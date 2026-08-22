package com.ssajudn.barebudget.ui.tour

import com.ssajudn.barebudget.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Locks the tour script integrity: every step must point to a real route and a
 * unique anchor key, otherwise the overlay silently never shows that step.
 */
class TourScriptTest {

    private val validRoutes = setOf(
        Screen.Dashboard.route,
        Screen.AddTransaction.route,
        Screen.AllTransactions.route,
        Screen.Analytics.route,
        Screen.Settings.route,
        Screen.DueBills.route,
        Screen.Transfer.route,
        Screen.Goals.route,
        Screen.Budget.route,
        Screen.Wallets.route
    )

    @Test
    fun `every step references a real navigation route`() {
        TourScript.steps.forEach { step ->
            assertTrue(
                "Step '${step.anchorKey}' has unknown route '${step.route}'",
                step.route in validRoutes
            )
        }
    }

    @Test
    fun `anchor keys are unique`() {
        val keys = TourScript.steps.map { it.anchorKey }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `every step has non-zero and unique string resources`() {
        val titleIds = TourScript.steps.map { it.titleRes }
        val descIds = TourScript.steps.map { it.descriptionRes }
        assertTrue(titleIds.all { it != 0 })
        assertTrue(descIds.all { it != 0 })
        assertEquals(titleIds.size, titleIds.toSet().size)
        assertEquals(descIds.size, descIds.toSet().size)
    }
}
