package com.ssajudn.bareuang.ui.imports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.presentation.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMutasiScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportMutasiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { eff ->
            when (eff) {
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(eff.message)
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(eff.uiText.asString(context))
                else -> {}
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFilePicked(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.drafts.isNotEmpty()) {
                val selectedCount = uiState.drafts.count { it.isSelected && !it.isDuplicate }
                val totalAmount = uiState.drafts.filter { it.isSelected && !it.isDuplicate }.sumOf { it.amount }
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.import_selected_count, selectedCount), style = MaterialTheme.typography.titleSmall)
                            Text(CurrencyFormatter.formatRupiah(totalAmount), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            enabled = selectedCount > 0 && !uiState.isImporting && uiState.selectedWalletId != null,
                            onClick = { viewModel.importSelected { onNavigateBack() } }
                        ) {
                            if (uiState.isImporting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.import_btn))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wallet selector
            var expanded by remember { mutableStateOf(false) }
            val selectedWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedWallet?.name ?: stringResource(R.string.import_wallet_label),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.import_wallet_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.wallets.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { viewModel.onWalletSelected(w.id!!); expanded = false })
                    }
                }
            }

            OutlinedButton(
                onClick = { picker.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.fileName != null) stringResource(R.string.import_change_file, uiState.fileName!!) else stringResource(R.string.import_pick_file))
            }
            if (uiState.wallets.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(stringResource(R.string.import_wallet_empty_desc), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (uiState.skippedRows > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(stringResource(R.string.import_skipped_banner, uiState.skippedRows), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }

            if (uiState.isParsing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.import_parsing), style = MaterialTheme.typography.bodySmall)
            }

            if (uiState.drafts.isEmpty() && !uiState.isParsing) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.import_supported_format), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.import_format_bca), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.import_format_generic), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.import_format_debit_credit), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.import_format_example), style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (uiState.drafts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.import_rows_found, uiState.drafts.size), style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.selectAll(true) }) { Text(stringResource(R.string.import_select_all)) }
                        TextButton(onClick = { viewModel.selectAll(false) }) { Text(stringResource(R.string.import_deselect_all)) }
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.drafts, key = { it.id }) { draft ->
                        ImportDraftRow(
                            draft = draft,
                            onToggle = { viewModel.onDraftToggle(draft.id) },
                            onCategoryChange = { viewModel.onDraftCategoryChange(draft.id, it) },
                            onTypeChange = { viewModel.onDraftTypeChange(draft.id, it) }
                        )
                    }
                }
            }
        }
    }

    // Soft daily-budget nudge: confirm before importing drafts over today's allowance.
    if (uiState.pendingDailyOverride) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDailyOverrideImport() },
            title = { Text(stringResource(R.string.tx_error_daily_exceeded_title)) },
            text = { Text(stringResource(R.string.import_daily_override_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDailyOverrideImport { onNavigateBack() } }) {
                    Text(stringResource(R.string.import_daily_override_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDailyOverrideImport() }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportDraftRow(
    draft: com.ssajudn.bareuang.domain.model.ImportDraft,
    onToggle: () -> Unit,
    onCategoryChange: (TransactionCategory) -> Unit,
    onTypeChange: (TransactionType) -> Unit
) {
    var catExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (draft.isDuplicate) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = draft.isSelected, onCheckedChange = { onToggle() }, enabled = !draft.isDuplicate)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(draft.merchant, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text("${draft.date} • ${CurrencyFormatter.formatRupiah(draft.amount)}", style = MaterialTheme.typography.bodySmall)
                if (draft.isDuplicate) {
                    Text(stringResource(R.string.import_duplicate_skip), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = draft.type == TransactionType.EXPENSE, onClick = { onTypeChange(TransactionType.EXPENSE) }, label = { Text(stringResource(R.string.import_chip_expense)) })
                        FilterChip(selected = draft.type == TransactionType.INCOME, onClick = { onTypeChange(TransactionType.INCOME) }, label = { Text(stringResource(R.string.import_chip_income)) })
                    }
                }
            }
            Box {
                AssistChip(onClick = { catExpanded = true }, label = { Text(draft.category.name) })
                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    TransactionCategory.entries.forEach { cat ->
                        DropdownMenuItem(text = { Text(stringResource(cat.labelRes())) }, onClick = { onCategoryChange(cat); catExpanded = false })
                    }
                }
            }
        }
    }
}
