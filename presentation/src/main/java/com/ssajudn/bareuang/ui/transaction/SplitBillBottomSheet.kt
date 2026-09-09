package com.ssajudn.bareuang.ui.transaction
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppOutlinedButton
import com.ssajudn.bareuang.ui.components.bareuangOutlinedTextFieldColors
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillBottomSheet(
    totalBillAmount: Long,
    merchantName: String,
    onDismiss: () -> Unit,
    onApplyMyPortion: (Long) -> Unit
) {
    val context = LocalContext.current
    val personDefaultTemplate = stringResource(R.string.split_person_default)
    val defaultMerchant = stringResource(R.string.split_default_merchant)
    var peopleCount by remember { mutableIntStateOf(2) }
    var taxPercentage by remember { mutableStateOf("0") }
    var servicePercentage by remember { mutableStateOf("0") }

    val meLabel = stringResource(R.string.split_person_me)
    var personNames by remember(meLabel) {
        mutableStateOf(
            listOf(meLabel, String.format(personDefaultTemplate, 1))
        )
    }

    LaunchedEffect(peopleCount) {
        val current = personNames.toMutableList()
        while (current.size < peopleCount) {
            current.add(String.format(personDefaultTemplate, current.size + 1))
        }
        while (current.size > peopleCount && current.size > 1) {
            current.removeAt(current.size - 1)
        }
        personNames = current
    }

    val taxPct = taxPercentage.toDoubleOrNull() ?: 0.0
    val svcPct = servicePercentage.toDoubleOrNull() ?: 0.0

    val taxAmount = (totalBillAmount * (taxPct / 100.0)).toLong()
    val serviceAmount = (totalBillAmount * (svcPct / 100.0)).toLong()
    val grandTotal = totalBillAmount + taxAmount + serviceAmount

    val perPersonAmount = if (peopleCount > 0) grandTotal / peopleCount else 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.split_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                AppIconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close))
                }
            }

            // Calculation Overview Card (M3 ElevatedCard)
            ElevatedCard(
                shape = AppShapes.LargeIncreased,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.split_subtotal), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            CurrencyFormatter.formatRupiah(totalBillAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    if (taxAmount > 0 || serviceAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.split_tax_service), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "+ ${CurrencyFormatter.formatRupiah(taxAmount + serviceAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.split_per_person),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.split_people_count) + " ($peopleCount)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = CurrencyFormatter.formatRupiah(perPersonAmount),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // People Stepper Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.split_people_count),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledIconButton(
                        onClick = { if (peopleCount > 2) peopleCount-- },
                        enabled = peopleCount > 2,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "$peopleCount",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    FilledIconButton(
                        onClick = { if (peopleCount < 20) peopleCount++ },
                        enabled = peopleCount < 20,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tax & Service Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = taxPercentage,
                    onValueChange = { taxPercentage = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text(stringResource(R.string.split_tax_service)) },
                    placeholder = { Text("10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f),
                    colors = bareuangOutlinedTextFieldColors()
                )

                OutlinedTextField(
                    value = servicePercentage,
                    onValueChange = { servicePercentage = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text(stringResource(R.string.split_service_label)) },
                    placeholder = { Text("5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f),
                    colors = bareuangOutlinedTextFieldColors()
                )
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share WhatsApp button
                AppOutlinedButton(
                    onClick = {
                        shareSplitToWhatsApp(
                            context = context,
                            merchant = merchantName.ifBlank { defaultMerchant },
                            total = grandTotal,
                            peopleCount = peopleCount,
                            perPerson = perPersonAmount,
                            names = personNames
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.split_share_whatsapp), fontWeight = FontWeight.SemiBold)
                }

                // Apply My Share only
                AppButton(
                    onClick = {
                        onApplyMyPortion(perPersonAmount)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        stringResource(R.string.split_apply, CurrencyFormatter.formatRupiah(perPersonAmount)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun shareSplitToWhatsApp(
    context: Context,
    merchant: String,
    total: Long,
    peopleCount: Int,
    perPerson: Long,
    names: List<String>
) {
    val builder = StringBuilder()
    builder.append("*Bareuang Split Bill - $merchant*\n")
    builder.append(context.getString(R.string.split_message_total, CurrencyFormatter.formatRupiah(total))+"\n")
    builder.append(context.getString(R.string.split_message_people, peopleCount)+"\n")
    builder.append(context.getString(R.string.split_message_per_person, CurrencyFormatter.formatRupiah(perPerson))+"\n")
    builder.append("------------------------------------\n")
    names.forEachIndexed { index, name ->
        builder.append("${index + 1}. $name: ${CurrencyFormatter.formatRupiah(perPerson)}\n")
    }
    builder.append("------------------------------------\n")
    builder.append(context.getString(R.string.split_message_auto))

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, builder.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.split_share_title))
    context.startActivity(shareIntent)
}
