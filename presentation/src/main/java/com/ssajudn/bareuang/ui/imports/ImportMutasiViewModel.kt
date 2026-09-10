package com.ssajudn.bareuang.ui.imports

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.CsvParserPort
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.BulkCreateTransactionsUseCase
import com.ssajudn.bareuang.domain.usecase.ParseMutasiCsvUseCase
import com.ssajudn.bareuang.domain.port.ImportPreferencesPort
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val drafts: List<ImportDraft> = emptyList(),
    val fileName: String? = null,
    val isParsing: Boolean = false,
    val isImporting: Boolean = false,
    val skippedRows: Int = 0,
    val error: UiText? = null,
    // Soft daily-budget nudge: some selected drafts exceed today's allowance.
    val pendingDailyOverride: Boolean = false
)

@HiltViewModel
class ImportMutasiViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walletRepository: WalletRepository,
    private val csvParser: CsvParserPort,
    private val parseMutasiCsvUseCase: ParseMutasiCsvUseCase,
    private val bulkCreate: BulkCreateTransactionsUseCase,
    private val importPrefs: ImportPreferencesPort
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                wallets = wallets,
                selectedWalletId = wallets.firstOrNull()?.id
            )
            walletRepository.observeWallets().collect { list ->
                _uiState.value = _uiState.value.copy(wallets = list)
            }
        }
    }

    fun onWalletSelected(id: String) {
        _uiState.value = _uiState.value.copy(selectedWalletId = id)
    }

    fun onDraftToggle(id: String) {
        _uiState.value = _uiState.value.copy(
            drafts = _uiState.value.drafts.map { if (it.id == id && !it.isDuplicate) it.copy(isSelected = !it.isSelected) else it }
        )
    }

    fun onDraftCategoryChange(id: String, category: TransactionCategory) {
        _uiState.value = _uiState.value.copy(drafts = _uiState.value.drafts.map { if (it.id == id) it.copy(category = category) else it })
    }

    fun onDraftTypeChange(id: String, type: TransactionType) {
        _uiState.value = _uiState.value.copy(drafts = _uiState.value.drafts.map { if (it.id == id) it.copy(type = type) else it })
    }

    fun selectAll(select: Boolean) {
        _uiState.value = _uiState.value.copy(
            drafts = _uiState.value.drafts.map { if (it.isDuplicate) it else it.copy(isSelected = select) }
        )
    }

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsing = true, error = null)
            _operation.value = OperationState.Loading
            try {
                // file size guard 5MB
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                val size = pfd?.statSize ?: -1L
                pfd?.close()
                if (size > 5 * 1024 * 1024) {
                    val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_file_too_large)
                    _uiState.value = _uiState.value.copy(isParsing = false, error = ui)
                    _operation.value = OperationState.Error("", ui)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    return@launch
                }
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                // DocumentFile display name
                var fileName = "mutasi.csv"
                context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst() && idx >= 0) fileName = c.getString(idx) ?: fileName
                }
                if (fileName.isBlank()) fileName = uri.lastPathSegment ?: "mutasi.csv"
                val (rawDrafts, skipped) = csvParser.parseWithStats(text)
                android.util.Log.d("Import", "parsed ${rawDrafts.size} drafts, skipped $skipped")
                if (rawDrafts.isEmpty()) {
                    val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_empty_csv)
                    _uiState.value = _uiState.value.copy(isParsing = false, fileName = fileName, skippedRows = skipped, error = ui)
                    _operation.value = OperationState.Error("", ui)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    return@launch
                }
                val result = parseMutasiCsvUseCase.markDuplicates(rawDrafts, skipped).getOrElse { throw it }
                _uiState.value = _uiState.value.copy(
                    drafts = result.drafts,
                    fileName = fileName,
                    isParsing = false,
                    skippedRows = result.skippedRows
                )
                _operation.value = OperationState.Idle
                if (result.skippedRows > 0) android.util.Log.w("Import", "skipped ${result.skippedRows} rows")
                if (result.duplicateCount > 0) {
                    _effect.send(UiEffect.ShowSnackbarRes(UiText.PluralRes(com.ssajudn.bareuang.presentation.R.plurals.import_duplicate_snack, result.duplicateCount, listOf(result.duplicateCount))))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("Import", "onFilePicked failed", e)
                val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_error_read)
                _uiState.value = _uiState.value.copy(isParsing = false, error = ui)
                _operation.value = OperationState.Error("", ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }

    fun importSelected(onSuccess: (Int) -> Unit) {
        val walletId = _uiState.value.selectedWalletId
        if (walletId.isNullOrBlank()) {
            viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required))) }
            return
        }
        doImport(force = false, onSuccess = onSuccess)
    }

    /** Proceed after the user accepts the daily-budget override prompt for a batch. */
    fun confirmDailyOverrideImport(onSuccess: (Int) -> Unit) {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false)
        doImport(force = true, onSuccess = onSuccess)
    }

    /** Cancel a daily-budget override prompt for a batch. */
    fun dismissDailyOverrideImport() {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false, isImporting = false)
        _operation.value = OperationState.Idle
    }

    private fun doImport(force: Boolean, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            _operation.value = OperationState.Loading
            val walletId = _uiState.value.selectedWalletId
            if (walletId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                _operation.value = OperationState.Idle
                return@launch
            }
            val res = bulkCreate(
                _uiState.value.drafts,
                walletId,
                com.ssajudn.bareuang.utils.CurrencyFormatter.getActiveCurrency(),
                force
            )
            res.onSuccess { count ->
                importPrefs.increment(count)
                android.util.Log.d("Import", "import success $count, total ${importPrefs.importCount.value}")
                _uiState.value = _uiState.value.copy(isImporting = false, drafts = emptyList(), fileName = null, skippedRows = 0)
                _operation.value = OperationState.Success("$count transaksi diimport")
                _effect.send(UiEffect.ShowSnackbar("$count transaksi berhasil diimport"))
                onSuccess(count)
            }.onFailure { e ->
                android.util.Log.e("Import", "import failed", e)
                if (e is AppException.DailyBudgetExceededException) {
                    // Soft nudge: ask before importing drafts that exceed today's allowance.
                    _uiState.value = _uiState.value.copy(isImporting = false, pendingDailyOverride = true)
                    return@launch
                }
                val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_error_save)
                _uiState.value = _uiState.value.copy(isImporting = false)
                _operation.value = OperationState.Error("", ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }

    fun clearDrafts() {
        _uiState.value = _uiState.value.copy(drafts = emptyList(), fileName = null)
    }
}
