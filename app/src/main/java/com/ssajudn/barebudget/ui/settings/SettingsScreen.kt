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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssajudn.barebudget.BuildConfig
import com.ssajudn.barebudget.data.local.ThemePreferences
import com.ssajudn.barebudget.utils.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    val context = LocalContext.current
    // viewModel { } rather than remember { }: a ViewModel created with remember is
    // not lifecycle-scoped, so it was destroyed and recreated on every
    // configuration change and its viewModelScope was not managed by the framework.
    val viewModel: SettingsViewModel = viewModel { SettingsViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    val themePrefs = remember { ThemePreferences.getInstance(context) }
    val colorMode by themePrefs.colorMode.collectAsState()
    val darkMode by themePrefs.darkMode.collectAsState()
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
                        text = "Pengaturan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                                text = "Google Account Connected",
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
                title = "Cadangan Data (Offline Backup)",
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = "Ekspor File Backup (Download)",
                        description = "Simpan semua transaksi, tagihan, dompet, dan target tabungan ke file JSON",
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            exportBackupLauncher.launch("BareBudget_Backup_$timeStamp.json")
                        }
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = "Impor File Backup (Restore)",
                        description = "Pulihkan seluruh data dari file JSON cadangan sebelumnya",
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
                    title = "Akun & Cloud Sync (Opsional)",
                    items = listOf(
                        com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                            title = "Hubungkan Akun Google",
                            description = "Sinkronkan data secara otomatis ke cloud",
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

            // 4. APPLICATION & PREFERENCES GROUP
            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = "Application & About",
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = "App Version",
                        description = "Current installed build",
                        value = BuildConfig.VERSION_NAME,
                        icon = Icons.Default.Info
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = "Environment",
                        description = "Active backend endpoint",
                        value = if (AppConfig.isDebug) "Development" else "Production",
                        icon = Icons.Default.Dns
                    )
                )
            )

            // 5. SUPPORT & APPRECIATION GROUP
            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = "Dukungan & Apresiasi",
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = "Donasi untuk Pengembang",
                        description = "Traktir kopi atau dukung kelanjutan BareBudget",
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
                        title = "Beri Bintang di GitHub",
                        description = "Beri star repository BareBudget di GitHub",
                        icon = Icons.Default.Star,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/ssajudn/BareBudget")
                            )
                            context.startActivity(intent)
                        }
                    ),
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = "Bagikan Aplikasi",
                        description = "Ajak teman untuk kelola keuangan & runway finansial",
                        icon = Icons.Default.Share,
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Kelola keuangan dan hitung runway finansialmu dengan BareBudget! Cek repository-nya di: https://github.com/ssajudn/BareBudget"
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan BareBudget"))
                        }
                    )
                )
            )

            // 5. DANGER ZONE GROUP
            com.ssajudn.barebudget.ui.components.Material3SettingsGroup(
                title = "Session & Danger Zone",
                items = listOf(
                    com.ssajudn.barebudget.ui.components.Material3SettingsItem(
                        title = if (uiState.isGuestMode) "Reset Local Data" else "Sign Out",
                        description = "Clear current session and return to onboarding",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        isDestructive = true,
                        onClick = { showSignOutConfirmDialog = true }
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSignOutConfirmDialog) {
        AppConfirmDialog(
            title = if (uiState.isGuestMode) "Reset Guest Session?" else "Sign Out?",
            message = if (uiState.isGuestMode) {
                "Are you sure you want to exit? Since you are in Guest Mode, your local transactions and records will be cleared."
            } else {
                "Are you sure you want to sign out from your Google account on this device?"
            },
            confirmButtonText = if (uiState.isGuestMode) "Reset" else "Keluar",
            onDismissRequest = { showSignOutConfirmDialog = false },
            onConfirm = {
                showSignOutConfirmDialog = false
                viewModel.signOut()
            }
        )
    }
}
