package com.ssajudn.bareuang.ui.settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.ui.components.AppConfirmDialog
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.BudgetWarningAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.PriceDisplayStyle
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.ui.theme.crispBorder
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.presentation.BuildConfig
import com.ssajudn.bareuang.domain.AppConfig
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onReplayTour: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToOcr: () -> Unit = {},
    onLocalDataReset: () -> Unit
) {
    val context = LocalContext.current
    // viewModel { } rather than remember { }: a ViewModel created with remember is
    // not lifecycle-scoped, so it was destroyed and recreated on every
    // configuration change and its viewModelScope was not managed by the framework.
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading

    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.isLocalDataReset) {
        if (uiState.isLocalDataReset) {
            onLocalDataReset()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(effect.uiText.asString(context))
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> onLocalDataReset()
            }
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importBackup(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(enabled = !isOperationLoading, onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. OFFLINE BACKUP & RESTORE GROUP
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.settings_backup_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_import_mutasi_title),
                        description = stringResource(R.string.settings_import_mutasi_desc),
                        icon = Icons.Default.UploadFile,
                        onClick = onNavigateToImport
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_ocr_title),
                        description = stringResource(R.string.settings_ocr_desc),
                        icon = Icons.Default.DocumentScanner,
                        value = stringResource(R.string.coming_soon),
                        onClick = null
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_export_title),
                        description = stringResource(R.string.settings_export_desc),
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            exportBackupLauncher.launch("Bareuang_Backup_$timeStamp.json")
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_import_title),
                        description = stringResource(R.string.settings_import_desc),
                        icon = Icons.Default.FileUpload,
                        onClick = {
                            importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        }
                    )
                )
            )

            AppearanceSettingsGroup(
                darkMode = darkMode,
                onDarkModeChange = viewModel::setDarkMode,
            )

            // 3b. WIDGET
            val widgetHideBalance by viewModel.widgetHideBalance.collectAsStateWithLifecycle()
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.settings_widget_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_widget_hide_balance),
                        description = stringResource(R.string.settings_widget_hide_balance_desc),
                        icon = Icons.Default.VisibilityOff,
                        onClick = { viewModel.setHideBalance(!widgetHideBalance) },
                        trailingContent = {
                            Switch(
                                checked = widgetHideBalance,
                                onCheckedChange = { viewModel.setHideBalance(it) }
                            )
                        }
                    )
                )
            )

            // 3b. BILL REMINDER TIME
            var reminderHour by remember { mutableIntStateOf(viewModel.reminderHour) }
            var reminderMinute by remember { mutableIntStateOf(viewModel.reminderMinute) }
            var showReminderTimeDialog by remember { mutableStateOf(false) }
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.settings_bill_reminder_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_bill_reminder_time),
                        description = stringResource(R.string.settings_bill_reminder_time_desc),
                        value = String.format(java.util.Locale.US, "%02d:%02d", reminderHour, reminderMinute),
                        icon = Icons.Default.NotificationsActive,
                        onClick = { showReminderTimeDialog = true }
                    )
                )
            )

            if (showReminderTimeDialog) {
                val timeState = rememberTimePickerState(
                    initialHour = reminderHour,
                    initialMinute = reminderMinute,
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showReminderTimeDialog = false },
                    title = {
                        Text(text = stringResource(R.string.settings_bill_reminder_time))
                    },
                    text = { TimePicker(state = timeState) },
                    confirmButton = {
                        AppTextButton(onClick = {
                            reminderHour = timeState.hour
                            reminderMinute = timeState.minute
                            viewModel.setReminderTime(timeState.hour, timeState.minute)
                            showReminderTimeDialog = false
                        }) {
                            Text(stringResource(R.string.common_save))
                        }
                    },
                    dismissButton = {
                        AppTextButton(onClick = { showReminderTimeDialog = false }) {
                            Text(stringResource(R.string.common_close))
                        }
                    }
                )
            }

            // 4. LANGUAGE SETTINGS
            var currentLanguage by remember { mutableStateOf(com.ssajudn.bareuang.utils.LanguageManager.getCurrentLanguageCode(context)) }
            var showLanguageDialog by remember { mutableStateOf(false) }

            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.language_settings),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.language_settings),
                        description = if (currentLanguage == "id") {
                            stringResource(R.string.language_indonesian)
                        } else {
                            stringResource(R.string.language_english)
                        },
                        icon = Icons.Default.Language,
                        onClick = { showLanguageDialog = true }
                    )
                )
            )

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = {
                        Text(text = stringResource(R.string.language_settings))
                    },
                    text = {
                        Column {
                            com.ssajudn.bareuang.utils.LanguageManager.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (currentLanguage == code),
                                        onClick = {
                                            currentLanguage = code
                                            com.ssajudn.bareuang.utils.LanguageManager.setLanguage(context, code)
                                            showLanguageDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        AppTextButton(onClick = { showLanguageDialog = false }) {
                            Text(stringResource(R.string.common_close))
                        }
                    }
                )
            }

            // 4b. CURRENCY SETTINGS
            val activeCurrency by viewModel.currency.collectAsStateWithLifecycle()
            var showCurrencyDialog by remember { mutableStateOf(false) }

            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.currency_settings),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.currency_settings),
                        description = if (activeCurrency == com.ssajudn.bareuang.domain.model.AppCurrency.IDR) {
                            stringResource(R.string.currency_idr)
                        } else {
                            stringResource(R.string.currency_usd)
                        },
                        icon = Icons.Default.Paid,
                        onClick = { showCurrencyDialog = true }
                    )
                )
            )

            if (showCurrencyDialog) {
                AlertDialog(
                    onDismissRequest = { showCurrencyDialog = false },
                    title = {
                        Text(text = stringResource(R.string.currency_settings))
                    },
                    text = {
                        Column {
                            com.ssajudn.bareuang.domain.model.AppCurrency.entries.forEach { curr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (activeCurrency == curr),
                                        onClick = {
                                            viewModel.setCurrency(curr)
                                            showCurrencyDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (curr == com.ssajudn.bareuang.domain.model.AppCurrency.IDR) {
                                            stringResource(R.string.currency_idr)
                                        } else {
                                            stringResource(R.string.currency_usd)
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        AppTextButton(onClick = { showCurrencyDialog = false }) {
                            Text(stringResource(R.string.common_close))
                        }
                    }
                )
            }

            // 4. SUPPORT & APPRECIATION GROUP
            val shareMessage = stringResource(R.string.settings_share_message)
            val shareChooser = stringResource(R.string.settings_share_chooser)
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.settings_support_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_replay_tour_title),
                        description = stringResource(R.string.settings_replay_tour_desc),
                        icon = Icons.Default.Tour,
                        onClick = {
                            viewModel.resetTour()
                            onReplayTour()
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_privacy_title),
                        description = stringResource(R.string.settings_privacy_desc),
                        icon = Icons.Default.Policy,
                        onClick = {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://bareuang.vercel.app/privacy")
                                )
                            )
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_donate_title),
                        description = stringResource(R.string.settings_donate_desc),
                        icon = Icons.Default.VolunteerActivism,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://trakteer.id/ssajudn")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_star_title),
                        description = stringResource(R.string.settings_star_desc),
                        icon = Icons.Default.Star,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/Udean777/Bare-Budget")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_share_title),
                        description = stringResource(R.string.settings_share_desc),
                        icon = Icons.Default.Share,
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, shareChooser))
                        }
                    )
                )
            )

            // 5. DANGER ZONE GROUP
            com.ssajudn.bareuang.ui.components.Material3SettingsGroup(
                title = stringResource(R.string.settings_danger_title),
                items = listOf(
                    com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                        title = stringResource(R.string.settings_reset_local),
                        description = stringResource(R.string.settings_danger_desc),
                        icon = Icons.AutoMirrored.Filled.Logout,
                        isDestructive = true,
                        onClick = { showResetConfirmDialog = true }
                    )
                )
            )

            // Minimalist Footer Versioning
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bareuang v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.settings_footer_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showResetConfirmDialog) {
        AppConfirmDialog(
            title = stringResource(R.string.settings_dialog_reset_title),
            message = stringResource(R.string.settings_dialog_reset_message),
            confirmButtonText = stringResource(R.string.settings_dialog_reset_confirm),
            onDismissRequest = { showResetConfirmDialog = false },
            onConfirm = {
                showResetConfirmDialog = false
                viewModel.resetLocalData()
            }
        )
    }
}
