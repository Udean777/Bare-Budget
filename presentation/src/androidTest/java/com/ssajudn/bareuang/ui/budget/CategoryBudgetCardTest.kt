package com.ssajudn.bareuang.ui.budget

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ssajudn.bareuang.domain.model.CategoryBudget
import com.ssajudn.bareuang.domain.model.TransactionCategory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryBudgetCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun card_displaysSpentLimitAndProgress() {
        composeRule.setContent {
            MaterialTheme {
                CategoryBudgetCard(
                    categoryBudget = CategoryBudget(
                        category = TransactionCategory.FOOD,
                        limitAmount = 1_000_000L,
                        spentAmount = 250_000L,
                    ),
                    onEdit = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("25%").assertExists()
        composeRule.onNodeWithText("25%").assertTextContains("25%")
    }
}
