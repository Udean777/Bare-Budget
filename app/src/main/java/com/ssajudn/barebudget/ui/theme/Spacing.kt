package com.ssajudn.barebudget.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing scale, in 4dp steps per the Material 3 layout grid.
 *
 * Exists because spacing was previously invented per screen: horizontal screen
 * padding was 20dp in some places and 24dp in others, and the gap left for the
 * floating navigation bar was 88dp on the dashboard but 100dp on three other
 * screens — for the same bar.
 *
 * A plain object, not a CompositionLocal: these values never vary by theme, and a
 * local would add indirection for nothing.
 */
object Spacing {
    /** 4dp — between tightly related items, e.g. a label and its value. */
    val ExtraSmall = 4.dp

    /** 8dp — icon-to-text, chip gaps. */
    val Small = 8.dp

    /** 12dp — inside compact containers. */
    val MediumSmall = 12.dp

    /** 16dp — the default. Card padding, list row insets, gaps between cards. */
    val Medium = 16.dp

    /** 20dp — generous card interiors. */
    val MediumLarge = 20.dp

    /** 24dp — between major sections. */
    val Large = 24.dp

    /** 32dp — around hero content. */
    val ExtraLarge = 32.dp

    /**
     * 16dp — horizontal inset from the screen edge.
     *
     * One value for every screen. Vertical list padding should come from the
     * Scaffold's inner padding, not a constant.
     */
    val ScreenHorizontal = 16.dp

    /**
     * 88dp — bottom padding so scrollable content can clear the FAB.
     *
     * A floating action button overlaps content by design, so the navigation bar
     * inset alone is not enough: the last list item would sit underneath it.
     * 56dp FAB + 16dp margin + 16dp breathing room.
     *
     * This is the *only* place a FAB/bar clearance number should appear. The nav
     * bar itself is handled by the Scaffold inset, not by a constant — screens
     * previously hardcoded 88dp and 100dp for that, which is what this replaces.
     */
    val FabClearance = 88.dp
}

/**
 * 48dp — the minimum touch-target size for any interactive element.
 *
 * From the Material accessibility guidance and WCAG 2.1 target-size guidance.
 * Anything tappable should be at least this large even when its icon is smaller;
 * shrink the icon, not the target.
 */
val MinTouchTarget = 48.dp
