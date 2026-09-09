package com.ssajudn.bareuang.ui.goals
import androidx.compose.material3.FilterChip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.Goal
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppFormDialog
import com.ssajudn.bareuang.ui.components.WalletDropdown
import com.ssajudn.bareuang.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositGoalDialog(
    goal: Goal,
    wallets: List<Wallet>,
    initialWithdraw: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, walletId: String) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var parsedAmount by remember { mutableStateOf(0L) }
    var isWithdraw by remember { mutableStateOf(initialWithdraw) }
    var selectedWallet by remember(wallets) { mutableStateOf(wallets.firstOrNull()) }

    val walletBalance = selectedWallet?.balance ?: 0L
    val isAmountValid = parsedAmount > 0
    val isBalanceValid = if (isWithdraw) parsedAmount <= goal.currentAmount else parsedAmount <= walletBalance
    val isFormValid = isAmountValid && isBalanceValid && selectedWallet?.id != null

    AppFormDialog(
        title = if (isWithdraw) stringResource(R.string.goals_withdraw_dialog) else stringResource(R.string.goals_deposit_dialog),
        icon = if (isWithdraw) Icons.Default.Payments else Icons.Default.Savings,
        iconTint = if (isWithdraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        confirmButtonText = if (isWithdraw) stringResource(R.string.goals_withdraw_btn) else stringResource(R.string.goals_deposit_btn),
        confirmButtonContainerColor = if (isWithdraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        isConfirmEnabled = isFormValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            selectedWallet?.id?.let { wId ->
                val finalAmount = if (isWithdraw) -parsedAmount else parsedAmount
                onConfirm(finalAmount, wId)
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isWithdraw,
                onClick = { isWithdraw = false },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text(stringResource(R.string.goals_chip_deposit)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isWithdraw,
                onClick = { isWithdraw = true },
                leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text(stringResource(R.string.goals_chip_withdraw)) },
                modifier = Modifier.weight(1f)
            )
        }

        // Wallet Selector Dropdown
        Text(
            text = if (isWithdraw) stringResource(R.string.goals_wallet_withdraw) else stringResource(R.string.goals_wallet_deposit),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        WalletDropdown(
            wallets = wallets,
            selectedWalletId = selectedWallet?.id,
            label = stringResource(R.string.goals_wallet_label),
            emptyText = stringResource(R.string.goals_wallet_choose),
            onSelected = { selectedWallet = it }
        )

        AmountTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input
                parsedAmount = input.toLongOrNull() ?: 0L
            },
            label = stringResource(R.string.goals_amount_rp),
            isError = parsedAmount > 0 && !isBalanceValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (parsedAmount > 0 && !isBalanceValid) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isWithdraw) stringResource(R.string.goals_error_exceed_goal, CurrencyFormatter.formatRupiah(goal.currentAmount))
                       else stringResource(R.string.goals_error_exceed_wallet, CurrencyFormatter.formatRupiah(walletBalance)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
