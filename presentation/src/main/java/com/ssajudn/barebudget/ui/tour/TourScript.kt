package com.ssajudn.barebudget.ui.tour

import androidx.annotation.StringRes
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.ui.navigation.Screen

data class TourStep(
    val route: String,
    val anchorKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

/**
 * Urutan tur mengikuti alur inti aplikasi:
 * lihat budget -> kenali dompet -> catat transaksi -> fitur pendukung.
 */
object TourScript {
    val steps: List<TourStep> = listOf(
        TourStep(
            route = Screen.Dashboard.route,
            anchorKey = "dashboard_runway",
            titleRes = R.string.tour_step1_title,
            descriptionRes = R.string.tour_step1_desc
        ),
        TourStep(
            route = Screen.Dashboard.route,
            anchorKey = "dashboard_quick_actions",
            titleRes = R.string.tour_step2_title,
            descriptionRes = R.string.tour_step2_desc
        ),
        TourStep(
            route = Screen.Budget.route,
            anchorKey = "budget_explainer",
            titleRes = R.string.tour_step3_title,
            descriptionRes = R.string.tour_step3_desc
        ),
        TourStep(
            route = Screen.Wallets.route,
            anchorKey = "wallets_summary",
            titleRes = R.string.tour_step4_title,
            descriptionRes = R.string.tour_step4_desc
        ),
        TourStep(
            route = Screen.Dashboard.route,
            anchorKey = "fab_add_transaction",
            titleRes = R.string.tour_step5_title,
            descriptionRes = R.string.tour_step5_desc
        ),
        TourStep(
            route = Screen.DueBills.route,
            anchorKey = "nav_bills",
            titleRes = R.string.tour_step6_title,
            descriptionRes = R.string.tour_step6_desc
        ),
        TourStep(
            route = Screen.Analytics.route,
            anchorKey = "nav_analytics",
            titleRes = R.string.tour_step7_title,
            descriptionRes = R.string.tour_step7_desc
        )
    )
}
