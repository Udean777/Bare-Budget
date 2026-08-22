package com.ssajudn.barebudget.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.barebudget.ui.components.AppConfirmDialog
import com.ssajudn.barebudget.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.ssajudn.barebudget.presentation.BuildConfig
import com.ssajudn.barebudget.data.local.ThemePreferences
import com.ssajudn.barebudget.domain.AppConfig
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onReplayTour: () -> Unit = {},
    onSignOutSuccess: () -> Unit
) {
    val context = LocalContext.current
    // viewModel { } rather than remember { }: a ViewModel created with remember is
    // not lifecycle-scoped, so it was destroyed and recreated on every
    // configuration change and its viewModelScope was not managed by the framework.
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading

    val themePrefs = remember { ThemePreferences.getInstance(context) }
    val tourPrefs = remember { com.ssajudn.barebudget.data.local.TourPreferences.getInstance(context) }
    val colorMode by themePrefs.colorMode.collectAsStateWithLifecycle()
    val darkMode by themePrefs.darkMode.collectAsStateWithLifecycle()
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onSignOutSuccess()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> onSignOutSuccess()
            }
        }
    }

    val exportBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importBackup(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(enabled = !isOperationLoading, onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.ssajudn.barebudget.presentation.R.string.common_back))
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

            // 1. USER PROFILE CARD (Hanya muncul jika login dengan Google)
            if (!uiState.isGuestMode) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = uiState.userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_google_connected),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 2. OFFLINE BACKUP & RESTORE GROUP
            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_backup_title),
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_export_title),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_export_desc),
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            exportBackupLauncher.launch("BareBudget_Backup_$timeStamp.json")
                        }
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_import_title),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_import_desc),
                        icon = Icons.Default.FileUpload,
                        onClick = {
                            importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        }
                    )
                )
            )

            // 3. CLOUD SYNC GROUP (Opsional untuk Guest yang ingin sync Google)
            if (uiState.isGuestMode) {
                com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                    title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_cloud_title),
                    items = listOf(
                        com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                            title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_link_google_title),
                            description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_link_google_desc),
                            icon = Icons.Default.CloudUpload,
                            onClick = { viewModel.linkWithGoogle() }
                        )
                    )
                )
            }

            // 3. APPEARANCE
            AppearanceSettingsGroup(
                colorMode = colorMode,
                darkMode = darkMode,
                onColorModeChange = themePrefs::setColorMode,
                onDarkModeChange = themePrefs::setDarkMode,
            )

            // 4. LANGUAGE SETTINGS
            var currentLanguage by remember { mutableStateOf(com.ssajudn.barebudget.utils.LanguageManager.getCurrentLanguageCode(context)) }
            var showLanguageDialog by remember { mutableStateOf(false) }

            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = androidx.compose.ui.res.stringResource(com.ssajudn.barebudget.presentation.R.string.language_settings),
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = androidx.compose.ui.res.stringResource(com.ssajudn.barebudget.presentation.R.string.language_settings),
                        description = if (currentLanguage == "id") {
                            androidx.compose.ui.res.stringResource(com.ssajudn.barebudget.presentation.R.string.language_indonesian)
                        } else {
                            androidx.compose.ui.res.stringResource(com.ssajudn.barebudget.presentation.R.string.language_english)
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
                        Text(text = androidx.compose.ui.res.stringResource(com.ssajudn.barebudget.presentation.R.string.language_settings))
                    },
                    text = {
                        Column {
                            com.ssajudn.barebudget.utils.LanguageManager.SUPPORTED_LANGUAGES.forEach { (code, label) ->
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
                                            com.ssajudn.barebudget.utils.LanguageManager.setLanguage(context, code)
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
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(stringResource(com.ssajudn.barebudget.presentation.R.string.common_close))
                        }
                    }
                )
            }

            // 4. SUPPORT & APPRECIATION GROUP
            val shareMessage = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_share_message)
            val shareChooser = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_share_chooser)
            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_support_title),
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_replay_tour_title),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_replay_tour_desc),
                        icon = Icons.Default.Tour,
                        onClick = {
                            tourPrefs.resetTour()
                            onReplayTour()
                        }
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_donate_title),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_donate_desc),
                        icon = Icons.Default.VolunteerActivism,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://trakteer.id/ssajudn")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_star_title),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_star_desc),
                        icon = Icons.Default.Star,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/Udean777/Bare-Budget")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_share_title),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_share_desc),
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
            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_danger_title),
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = if (uiState.isGuestMode) stringResource(com.ssajudn.barebudget.presentation.R.string.settings_reset_local) else stringResource(com.ssajudn.barebudget.presentation.R.string.settings_sign_out),
                        description = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_danger_desc),
                        icon = Icons.AutoMirrored.Filled.Logout,
                        isDestructive = true,
                        onClick = { showSignOutConfirmDialog = true }
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
                    text = "BareBudget v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(com.ssajudn.barebudget.presentation.R.string.settings_footer_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSignOutConfirmDialog) {
        AppConfirmDialog(
            title = if (uiState.isGuestMode) stringResource(com.ssajudn.barebudget.presentation.R.string.settings_dialog_reset_title) else stringResource(com.ssajudn.barebudget.presentation.R.string.settings_dialog_signout_title),
            message = if (uiState.isGuestMode) {
                stringResource(com.ssajudn.barebudget.presentation.R.string.settings_dialog_reset_message)
            } else {
                stringResource(com.ssajudn.barebudget.presentation.R.string.settings_dialog_signout_message)
            },
            confirmButtonText = if (uiState.isGuestMode) stringResource(com.ssajudn.barebudget.presentation.R.string.settings_dialog_reset_confirm) else stringResource(com.ssajudn.barebudget.presentation.R.string.settings_dialog_signout_confirm),
            onDismissRequest = { showSignOutConfirmDialog = false },
            onConfirm = {
                showSignOutConfirmDialog = false
                viewModel.signOut()
            }
        )
    }
}
