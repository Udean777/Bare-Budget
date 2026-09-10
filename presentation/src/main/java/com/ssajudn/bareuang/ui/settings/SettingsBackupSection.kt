package com.ssajudn.bareuang.ui.settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.core.net.toUri

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.ssajudn.bareuang.ui.components.FeatureTopAppBar
import com.ssajudn.bareuang.ui.components.AppTextButton

@Composable
internal fun SettingsBackupSection(
    viewModel: SettingsViewModel,
    darkMode: com.ssajudn.bareuang.domain.model.AppThemeDarkMode,
    onDarkModeChange: (com.ssajudn.bareuang.domain.model.AppThemeDarkMode) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
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
                    onExportBackup()
                }
            ),
            com.ssajudn.bareuang.ui.components.Material3SettingsItem(
                title = stringResource(R.string.settings_import_title),
                description = stringResource(R.string.settings_import_desc),
                icon = Icons.Default.FileUpload,
                onClick = {
                    onImportBackup()
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
    
}

