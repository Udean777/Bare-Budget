package com.ssajudn.barebudget.ui.wallets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssajudn.barebudget.data.model.Wallet
import com.ssajudn.barebudget.ui.components.AppConfirmDialog
import com.ssajudn.barebudget.ui.components.AppFormDialog
import com.ssajudn.barebudget.ui.theme.Spacing
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dompet Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Dompet") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.ScreenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(Spacing.Medium))

            // Net Worth Summary Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Kekayaan (Net Worth)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))
                    Text(
                        text = CurrencyFormatter.formatRupiah(uiState.netWorth),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Large))

            Text(
                text = "Daftar Dompet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.Medium))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
                contentPadding = PaddingValues(bottom = Spacing.FabClearance)
            ) {
                items(uiState.wallets, key = { it.id ?: "" }) { wallet ->
                    WalletItem(
                        wallet = wallet,
                        onDelete = { viewModel.deleteWallet(wallet.id!!) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWalletDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, balance, color ->
                viewModel.addWallet(name, balance, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WalletItem(
    wallet: Wallet,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(wallet.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(parsedColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = parsedColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.Medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(wallet.balance),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus Dompet",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AppConfirmDialog(
            title = "Hapus Dompet?",
            message = "Apakah Anda yakin ingin menghapus '${wallet.name}'? Histori transaksinya mungkin akan kehilangan referensi dompet ini.",
            confirmButtonText = "Hapus",
            onDismissRequest = { showDeleteConfirm = false },
            onConfirm = {
                onDelete()
                showDeleteConfirm = false
            }
        )
    }
}

@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var balanceStr by remember { mutableStateOf("") }
    val colors = listOf("#2ECC71", "#3498DB", "#9B59B6", "#E67E22", "#E74C3C", "#34495E")
    var selectedColor by remember { mutableStateOf(colors.first()) }

    AppFormDialog(
        title = "Tambah Dompet",
        icon = Icons.Default.AccountBalanceWallet,
        onDismissRequest = onDismiss,
        onConfirm = {
            val amt = balanceStr.replace("[^\\d]".toRegex(), "").toLongOrNull() ?: 0L
            onConfirm(name, amt, selectedColor)
        },
        isConfirmEnabled = name.isNotBlank() && balanceStr.isNotBlank()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Dompet (mis. BCA, Tunai)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = balanceStr,
            onValueChange = { newValue ->
                val cleanString = newValue.replace("[^\\d]".toRegex(), "")
                if (cleanString.length <= 15) {
                    balanceStr = cleanString
                }
            },
            label = { Text("Saldo Awal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = CurrencyVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        
        Spacer(modifier = Modifier.height(Spacing.Small))
        
        Text(
            text = "Warna Dompet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colors.forEach { hex ->
                val color = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == hex) 3.dp else 0.dp,
                            color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = hex }
                )
            }
        }
    }
}
