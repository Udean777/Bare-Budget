package com.ssajudn.barebudget.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.data.auth.AuthResult
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import com.ssajudn.barebudget.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isGuestLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val operation by authViewModel.operation.collectAsState()
    val isOperationLoading = operation is OperationState.Loading

    val anyLoading = isGoogleLoading || isGuestLoading || isOperationLoading

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.Navigate -> onAuthSuccess()
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon / Official Logo
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "BareBudget Logo",
                modifier = Modifier
                    .size(88.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome to Bare Budget",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to sync your cloud records or continue instantly with a guest profile.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 1. SIGN IN WITH GOOGLE BUTTON
            Button(
                onClick = {
                    isGoogleLoading = true
                    authViewModel.signInWithGoogle { result ->
                        isGoogleLoading = false
                        when (result) {
                            is AuthResult.Success -> onAuthSuccess()
                            is AuthResult.Error -> errorMessage = result.message
                            is AuthResult.Cancelled -> { /* No-op */ }
                        }
                    }
                },
                enabled = !anyLoading && !isOperationLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sign In with Google", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. CONTINUE AS GUEST BUTTON
            OutlinedButton(
                onClick = {
                    isGuestLoading = true
                    authViewModel.signInAnonymously {
                        isGuestLoading = false
                        onAuthSuccess()
                    }
                },
                enabled = !anyLoading && !isOperationLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isGuestLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue as Guest", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
