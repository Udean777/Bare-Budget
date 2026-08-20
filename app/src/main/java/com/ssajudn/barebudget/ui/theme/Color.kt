package com.ssajudn.barebudget.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ssajudn.barebudget.data.model.TransactionCategory

@Immutable
class CategoryColors internal constructor(
    private val accentSet: CategoryColorSet,
    private val containerSet: CategoryColorSet,
    private val onContainerSet: CategoryColorSet,
) {
    fun accent(category: TransactionCategory): Color = accentSet.pick(category)
    fun container(category: TransactionCategory): Color = containerSet.pick(category)
    fun onContainer(category: TransactionCategory): Color = onContainerSet.pick(category)
}

private fun CategoryColorSet.pick(category: TransactionCategory): Color = when (category) {
    TransactionCategory.FOOD -> food
    TransactionCategory.TRANSPORT -> transport
    TransactionCategory.BILLS -> bills
    TransactionCategory.SHOPPING -> shopping
    TransactionCategory.ENTERTAINMENT -> entertainment
    TransactionCategory.SOCIAL -> social
    TransactionCategory.OTHER -> other
    else -> other
}

internal data class CategoryColorSet(
    val food: Color,
    val transport: Color,
    val bills: Color,
    val shopping: Color,
    val entertainment: Color,
    val social: Color,
    val other: Color,
)

// Adapted to blend with the Red theme. 
internal val LightCategoryAccent = CategoryColorSet(
    food = Color(0xFFC0392B),
    transport = Color(0xFF2980B9),
    bills = Color(0xFFD35400),
    shopping = Color(0xFF27AE60),
    entertainment = Color(0xFF8E44AD),
    social = Color(0xFF16A085),
    other = Color(0xFF7F8C8D),
)

internal val DarkCategoryAccent = CategoryColorSet(
    food = Color(0xFFE74C3C),
    transport = Color(0xFF3498DB),
    bills = Color(0xFFE67E22),
    shopping = Color(0xFF2ECC71),
    entertainment = Color(0xFF9B59B6),
    social = Color(0xFF1ABC9C),
    other = Color(0xFFBDC3C7),
)

internal val LightCategoryContainer = CategoryColorSet(
    food = Color(0xFFFADBD8),
    transport = Color(0xFFD4E6F1),
    bills = Color(0xFFF5CBA7),
    shopping = Color(0xFFD5F5E3),
    entertainment = Color(0xFFEBDEF0),
    social = Color(0xFFD1F2EB),
    other = Color(0xFFE5E7E9),
)

internal val DarkCategoryContainer = CategoryColorSet(
    food = Color(0xFF7B241C),
    transport = Color(0xFF1A5276),
    bills = Color(0xFF873600),
    shopping = Color(0xFF186A3B),
    entertainment = Color(0xFF512E5F),
    social = Color(0xFF0E6251),
    other = Color(0xFF515A5A),
)

internal val LightCategoryOnContainer = CategoryColorSet(
    food = Color(0xFF641E16),
    transport = Color(0xFF154360),
    bills = Color(0xFF6E2C00),
    shopping = Color(0xFF145A32),
    entertainment = Color(0xFF4A235A),
    social = Color(0xFF0B5345),
    other = Color(0xFF424949),
)

internal val DarkCategoryOnContainer = CategoryColorSet(
    food = Color(0xFFFADBD8),
    transport = Color(0xFFD4E6F1),
    bills = Color(0xFFF5CBA7),
    shopping = Color(0xFFD5F5E3),
    entertainment = Color(0xFFEBDEF0),
    social = Color(0xFFD1F2EB),
    other = Color(0xFFE5E7E9),
)

internal val LightCategoryColors = CategoryColors(
    accentSet = LightCategoryAccent,
    containerSet = LightCategoryContainer,
    onContainerSet = LightCategoryOnContainer,
)

internal val DarkCategoryColors = CategoryColors(
    accentSet = DarkCategoryAccent,
    containerSet = DarkCategoryContainer,
    onContainerSet = DarkCategoryOnContainer,
)
