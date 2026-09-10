package com.ssajudn.bareuang.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.components.WalletDropdown

@Composable
internal fun AddTransactionBudgetBanner(
    visible: Boolean,
    onNavigateToBudget: () -> Unit
) {
    if (!visible) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tx_budget_not_set),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            AppTextButton(onClick = onNavigateToBudget) {
                Text(
                    stringResource(R.string.tx_budget_set_action),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun AddTransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        listOf(
            TransactionType.EXPENSE to stringResource(R.string.tx_expense),
            TransactionType.INCOME to stringResource(R.string.tx_income),
            TransactionType.TRANSFER to stringResource(R.string.tx_transfer)
        ).forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
            ) { Text(label) }
        }
    }
}

@Composable
internal fun AddTransactionWalletSelector(
    state: AddTransactionUiState,
    onWalletSelected: (String) -> Unit,
    onDestinationWalletSelected: (String) -> Unit
) {
    if (state.transactionType == TransactionType.TRANSFER) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WalletDropdown(
                wallets = state.wallets,
                selectedWalletId = state.selectedWalletId,
                label = stringResource(R.string.tx_from_wallet),
                emptyText = stringResource(R.string.common_add),
                modifier = Modifier.weight(1f),
                onSelected = { it.id?.let(onWalletSelected) }
            )
            WalletDropdown(
                wallets = state.wallets,
                selectedWalletId = state.selectedToWalletId,
                label = stringResource(R.string.tx_to_wallet),
                emptyText = stringResource(R.string.common_add),
                modifier = Modifier.weight(1f),
                onSelected = { it.id?.let(onDestinationWalletSelected) }
            )
        }
    } else {
        WalletDropdown(
            wallets = state.wallets,
            selectedWalletId = state.selectedWalletId,
            label = stringResource(R.string.tx_wallet_label),
            onSelected = { it.id?.let(onWalletSelected) }
        )
    }
}
