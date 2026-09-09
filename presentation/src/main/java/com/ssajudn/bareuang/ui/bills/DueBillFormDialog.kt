package com.ssajudn.bareuang.ui.bills
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.ui.common.labelRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.ui.components.AppFormDialog
import com.ssajudn.bareuang.ui.components.bareuangOutlinedTextFieldColors
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueBillFormDialog(
    initialBill: DueBill? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        provider: String,
        providerIconUrl: String?,
        amount: Long,
        dueDate: String,
        isRecurring: Boolean,
        interval: RecurringInterval,
        notes: String
    ) -> Unit
) {
    val builtinProviders = BillProviderCatalog.builtin

    val existingProvider = builtinProviders.find { it.name == initialBill?.providerName }
    val initialSelectedProvider = existingProvider ?: if (initialBill != null) builtinProviders.last() else builtinProviders[0]

    var selectedProvider by remember { mutableStateOf(initialSelectedProvider) }
    var customProviderName by remember { mutableStateOf(if (existingProvider == null && initialBill != null) initialBill.providerName else "") }
    var customProviderIconUrl by remember { mutableStateOf<String?>(initialBill?.providerIconUrl) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            // Persist the picked image to internal storage so the icon remains accessible across app restarts
            customProviderIconUrl = BillProviderCatalog.persistPickedImage(context, uri) ?: uri.toString()
        }
    }

    var rawAmount by remember { mutableStateOf(initialBill?.totalAmount?.toString() ?: "") }
    var parsedAmount by remember { mutableStateOf(initialBill?.totalAmount ?: 0L) }
    var dueDateIso by remember { mutableStateOf(initialBill?.dueDate ?: DateUtils.getCurrentDateISO()) }
    var isRecurring by remember { mutableStateOf(initialBill?.isRecurring ?: false) }
    var recurringInterval by remember { mutableStateOf(initialBill?.recurringInterval ?: RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf(initialBill?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = DateUtils.parseIsoToMillis(dueDateIso),
            onDateSelected = { millis ->
                dueDateIso = DateUtils.formatMillisToIso(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    val finalProviderName = if (selectedProvider.isCustom) customProviderName.trim() else selectedProvider.name
    // Store drawable entry name (e.g., "logo_shopee") instead of raw integer resource ID for stability across builds
    val finalIconUrl = if (selectedProvider.isCustom) customProviderIconUrl else selectedProvider.iconRes?.let {
        BillProviderCatalog.toResUrl(context, it)
    }

    val isFormValid = finalProviderName.isNotBlank() && parsedAmount > 0

    AppFormDialog(
        title = if (initialBill != null) stringResource(R.string.bills_form_edit) else stringResource(R.string.bills_form_new),
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = if (initialBill != null) stringResource(R.string.bills_form_save) else stringResource(R.string.bills_form_add),
        isConfirmEnabled = isFormValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            onConfirm(
                finalProviderName,
                finalIconUrl,
                parsedAmount,
                dueDateIso,
                isRecurring,
                if (isRecurring) recurringInterval else RecurringInterval.NONE,
                notes
            )
        }
    ) {
        // Provider Selection Dropdown (Combobox)
        var providerDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = providerDropdownExpanded,
            onExpandedChange = { providerDropdownExpanded = !providerDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (selectedProvider.isCustom) stringResource(R.string.bills_provider_custom) else selectedProvider.name,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.bills_provider_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                leadingIcon = {
                    if (selectedProvider.iconRes != null) {
                        Image(
                            painter = painterResource(id = selectedProvider.iconRes!!),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                        )
                    } else if (selectedProvider.isCustom && customProviderIconUrl != null) {
                        val previewFile: java.io.File? =
                            customProviderIconUrl!!.takeIf { it.startsWith("/") }?.let { java.io.File(it) }
                        if (previewFile != null) {
                            LocalProviderIcon(model = previewFile, size = 24.dp)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = providerDropdownExpanded,
                onDismissRequest = { providerDropdownExpanded = false }
            ) {
                builtinProviders.forEach { provider ->
                    DropdownMenuItem(
                        leadingIcon = {
                            if (provider.iconRes != null) {
                                Image(
                                    painter = painterResource(id = provider.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        text = {
                            Text(
                                text = if (provider.isCustom) stringResource(R.string.bills_provider_custom) else provider.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedProvider == provider) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedProvider = provider
                            providerDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Custom Provider Input
        if (selectedProvider.isCustom) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = customProviderName,
                onValueChange = { customProviderName = it.take(100) },
                label = { Text(stringResource(R.string.bills_name_label)) },
                placeholder = { Text(stringResource(R.string.bills_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = bareuangOutlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (customProviderIconUrl != null) stringResource(R.string.bills_change_icon) else stringResource(R.string.bills_upload_icon))
            }
        }

        // Amount Input
        AmountTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input
                parsedAmount = input.toLongOrNull() ?: 0L
            },
            label = stringResource(R.string.bills_amount_label),
            modifier = Modifier.fillMaxWidth()
        )

        // Due Date Picker Field
        OutlinedTextField(
            value = DateFormatter.formatDisplayDate(dueDateIso),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.bills_due_date)) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.bills_due_date))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = bareuangOutlinedTextFieldColors()
        )

        // Recurring Switch — placed above Notes so the feature is discoverable without scrolling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.bills_recurring),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = stringResource(R.string.bills_recurring_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it }
            )
        }

        if (isRecurring) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(RecurringInterval.WEEKLY, RecurringInterval.MONTHLY, RecurringInterval.YEARLY).forEach { interval ->
                    FilterChip(
                        selected = recurringInterval == interval,
                        onClick = { recurringInterval = interval },
                        label = { Text(stringResource(interval.labelRes()), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Day of week selector when WEEKLY is selected
            if (recurringInterval == RecurringInterval.WEEKLY) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.tx_recurring_on_day),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val days = listOf(
                    1 to stringResource(R.string.day_mon),
                    2 to stringResource(R.string.day_tue),
                    3 to stringResource(R.string.day_wed),
                    4 to stringResource(R.string.day_thu),
                    5 to stringResource(R.string.day_fri),
                    6 to stringResource(R.string.day_sat),
                    7 to stringResource(R.string.day_sun)
                )
                val currentDayOfWeek = DateUtils.getDayOfWeek(dueDateIso)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { (dayIso, dayName) ->
                        val isCurrentDay = currentDayOfWeek == dayIso
                        Surface(
                            shape = AppShapes.Pill,
                            color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    dueDateIso = DateUtils.calculateNextWeeklyDay(dueDateIso, dayIso)
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isCurrentDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (recurringInterval == RecurringInterval.MONTHLY) {
                Spacer(modifier = Modifier.height(4.dp))
                val dayOfMonth = DateUtils.getDayOfMonth(dueDateIso)
                Text(
                    text = stringResource(R.string.tx_recurring_on_date, dayOfMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Notes Input (optional, last)
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it.take(500) },
            label = { Text(stringResource(R.string.bills_notes_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = bareuangOutlinedTextFieldColors()
        )
    }
}
