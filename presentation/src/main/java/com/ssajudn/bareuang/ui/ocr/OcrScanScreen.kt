package com.ssajudn.bareuang.ui.ocr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBar

import androidx.activity.result.PickVisualMediaRequest
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter
import java.io.File

private const val PRIVACY_POLICY_URL = "https://bareuang.vercel.app/privacy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: OcrScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val ocrEnabled = false

    LaunchedEffect(Unit) {
        viewModel.effect.collect { eff ->
            when (eff) {
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(eff.message)
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(eff.uiText.asString(context))
                else -> {}
            }
        }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingScanAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun openPrivacyPolicy() {
        context.startActivity(
            android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                Uri.parse(PRIVACY_POLICY_URL)
            )
        )
    }

    fun runAfterOcrConsent(action: () -> Unit) {
        if (uiState.hasOcrConsent) {
            action()
        } else {
            pendingScanAction = action
            viewModel.requestOcrConsent()
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { viewModel.processImage(it) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.processImage(it) }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ocr_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.rawText != null) {
                Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Button(
                        onClick = { viewModel.save { onNavigateBack() } },
                        enabled = !uiState.isSaving && uiState.parsedAmount > 0 && uiState.selectedWalletId != null,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text(stringResource(R.string.ocr_save_transaction, CurrencyFormatter.formatRupiah(uiState.parsedAmount)))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(com.ssajudn.bareuang.presentation.R.string.ocr_coming_soon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Wallet selector
            var expanded by remember { mutableStateOf(false) }
            val selectedWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedWallet?.name ?: "Pilih dompet",
                    onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.ocr_wallet_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.wallets.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { viewModel.onWalletSelected(w.id!!); expanded = false })
                    }
                }
            }

            if (!uiState.isOnline) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ocr_offline_message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { runAfterOcrConsent(::launchCamera) },
                    enabled = ocrEnabled && uiState.isOnline && !uiState.isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.ocr_btn_camera))
                }
                OutlinedButton(
                    onClick = {
                        runAfterOcrConsent {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    },
                    enabled = ocrEnabled && uiState.isOnline && !uiState.isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.ocr_btn_gallery))
                }
            }

            if (uiState.isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.ocr_processing), style = MaterialTheme.typography.bodySmall)
            }

            if (uiState.rawText == null && !uiState.isProcessing) {
                // Empty state with mini receipt illustration
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Mini blank receipt preview
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(140.dp)
                                .shadow(4.dp, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFFEF8))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0)))
                                Box(modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFEEEEEE)))
                                DashedDivider()
                                repeat(3) { Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF5F5F5))) }
                                DashedDivider()
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFFFECB3)))
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.ocr_tips_title), style = MaterialTheme.typography.titleSmall)
                                Text(stringResource(R.string.ocr_tip_lighting), style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.ocr_tip_total), style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.ocr_tip_editable), style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.ocr_tip_internet), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (uiState.rawText != null) {
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = viewModel::onMerchantChange,
                    label = { Text(stringResource(R.string.ocr_merchant_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                AmountTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = stringResource(R.string.ocr_amount_label),
                    modifier = Modifier.fillMaxWidth()
                )
                // Category
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(
                        value = stringResource(uiState.category.labelRes()),
                        onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.ocr_category_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        TransactionCategory.entries.forEach { cat ->
                            DropdownMenuItem(text = { Text(stringResource(cat.labelRes())) }, onClick = { viewModel.onCategoryChange(cat); catExpanded = false })
                        }
                    }
                }
                // Date
                var showDatePicker by remember { mutableStateOf(false) }
                if (showDatePicker) {
                    AppDatePickerDialog(
                        initialDateMillis = DateUtils.parseIsoToMillis(uiState.date),
                        onDateSelected = { viewModel.onDateChange(DateUtils.formatMillisToIso(it)) },
                        onDismiss = { showDatePicker = false }
                    )
                }
                OutlinedTextField(
                    value = DateFormatter.formatDisplayDate(uiState.date),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.ocr_date_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) { Text(stringResource(R.string.ocr_change)) }
                    }
                )
                OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.ocr_rescan)) }

                ReceiptPaperCard(
                    rawText = uiState.rawText!!,
                    merchant = uiState.merchant,
                    total = uiState.parsedAmount
                )
            }
        }
    }

    // Soft daily-budget nudge: confirm before saving a receipt over today's allowance.
    if (uiState.pendingDailyOverride) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDailyOverride() },
            title = { Text(stringResource(R.string.ocr_daily_override_title)) },
            text = { Text(uiState.pendingDailyMessage ?: "Jatah harian habis. Tetap simpan?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDailyOverride { onNavigateBack() } }) {
                    Text(stringResource(R.string.ocr_save_anyway))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDailyOverride() }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (uiState.showOcrConsent) {
        AlertDialog(
            onDismissRequest = {
                pendingScanAction = null
                viewModel.dismissOcrConsent()
            },
            title = { Text(stringResource(com.ssajudn.bareuang.presentation.R.string.ocr_consent_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(com.ssajudn.bareuang.presentation.R.string.ocr_consent_message))
                    TextButton(onClick = ::openPrivacyPolicy) {
                        Text(stringResource(com.ssajudn.bareuang.presentation.R.string.privacy_policy_link))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.acceptOcrConsent()
                    val action = pendingScanAction
                    pendingScanAction = null
                    action?.invoke()
                }) {
                    Text(stringResource(com.ssajudn.bareuang.presentation.R.string.ocr_consent_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingScanAction = null
                    viewModel.dismissOcrConsent()
                }) {
                    Text(stringResource(com.ssajudn.bareuang.presentation.R.string.common_cancel))
                }
            }
        )
    }
}
