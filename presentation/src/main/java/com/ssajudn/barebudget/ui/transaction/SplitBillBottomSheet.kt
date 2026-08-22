package com.ssajudn.barebudget.ui.transaction

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.barebudget.ui.theme.AppShapes
import com.ssajudn.barebudget.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillBottomSheet(
    totalBillAmount: Long,
    merchantName: String,
    onDismiss: () -> Unit,
    onApplyMyPortion: (Long) -> Unit
) {
    val context = LocalContext.current
    var peopleCount by remember { mutableIntStateOf(2) }
    var taxPercentage by remember { mutableStateOf("0") }
    var servicePercentage by remember { mutableStateOf("0") }

    var personNames by remember {
        mutableStateOf(
            listOf("Saya (Me)", "Teman 1")
        )
    }

    LaunchedEffect(peopleCount) {
        val current = personNames.toMutableList()
        while (current.size < peopleCount) {
            current.add("Teman ${current.size}")
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Split Bill",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
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
                        Text("Subtotal Tagihan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("Pajak & Service", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                text = "Porsi per Orang",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Bagi rata ($peopleCount orang)",
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
                    text = "Jumlah Orang",
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
                    label = { Text("Pajak PB1 (%)") },
                    placeholder = { Text("10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = servicePercentage,
                    onValueChange = { servicePercentage = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Service (%)") },
                    placeholder = { Text("5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share WhatsApp button
                OutlinedButton(
                    onClick = {
                        shareSplitToWhatsApp(
                            context = context,
                            merchant = merchantName.ifBlank { "Patungan" },
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
                    Text("Bagikan Rincian ke WhatsApp", fontWeight = FontWeight.SemiBold)
                }

                // Apply My Share only
                Button(
                    onClick = {
                        onApplyMyPortion(perPersonAmount)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "Terapkan Porsi Saya (${CurrencyFormatter.formatRupiah(perPersonAmount)})",
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
    builder.append("*BareBudget Split Bill - $merchant*\n")
    builder.append("Total Tagihan: ${CurrencyFormatter.formatRupiah(total)}\n")
    builder.append("Jumlah Orang: $peopleCount\n")
    builder.append("Tagihan per Orang: *${CurrencyFormatter.formatRupiah(perPerson)}*\n")
    builder.append("------------------------------------\n")
    names.forEachIndexed { index, name ->
        builder.append("${index + 1}. $name: ${CurrencyFormatter.formatRupiah(perPerson)}\n")
    }
    builder.append("------------------------------------\n")
    builder.append("Dihitung otomatis dengan BareBudget")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, builder.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan rincian split bill:")
    context.startActivity(shareIntent)
}
