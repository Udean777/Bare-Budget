package com.ssajudn.barebudget.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A top-level destination in [AppNavigationBar].
 *
 * @param selectedIcon optional filled variant shown while selected. M3 pairs an
 *   outlined icon when unselected with a filled one when selected; omit it and
 *   [icon] is used for both.
 */
data class NavigationBarItemData(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
)

/**
 * The app's bottom navigation bar, built on Material 3's [ShortNavigationBar].
 *
 * Replaces a hand-rolled version that put real [ShortNavigationBarItem]s
 * (previously `NavigationBarItem`s) inside a custom `Surface` + `Row`. That had
 * three concrete problems this fixes:
 *
 *  1. The container forced `height(72.dp)` on items that want 80dp, so the
 *     selected indicator and label could clip.
 *  2. `Arrangement.SpaceEvenly` with no weights sized items to their content, so
 *     "Analytics" got a larger touch target than "Home".
 *     [ShortNavigationBarArrangement.EqualWeight] gives every item an identical
 *     width, keeping all four above the 48dp minimum.
 *  3. Bypassing the real bar meant losing its window-inset handling, which was
 *     patched with a manual `navigationBarsPadding()`. That in turn forced
 *     `AppNavigation` to discard the Scaffold's inner padding and made each
 *     screen guess a bottom inset — 88dp on one screen, 100dp on three others.
 *     The bar now consumes its own insets, so screens can just use the padding
 *     the Scaffold gives them.
 */
@Composable
fun AppNavigationBar(
    items: List<NavigationBarItemData>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShortNavigationBar(
        modifier = modifier,
        arrangement = ShortNavigationBarArrangement.EqualWeight,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            ShortNavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon ?: item.icon else item.icon,
                        // Null: the label below already names the destination, and
                        // the item merges its children into one node. A description
                        // here would make screen readers announce the name twice.
                        contentDescription = null,
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}
