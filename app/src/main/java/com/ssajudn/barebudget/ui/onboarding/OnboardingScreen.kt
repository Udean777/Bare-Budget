package com.ssajudn.barebudget.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.barebudget.data.auth.AuthManager
import com.ssajudn.barebudget.data.auth.AuthResult
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isGuestLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    val pages = listOf(
        OnboardingPageData(
            title = "Zero Friction Expense Tracking",
            description = "Log your daily expenses in seconds. Clean, minimal, and focused on what truly matters to your wallet.",
            icon = Icons.Default.Bolt,
            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        OnboardingPageData(
            title = "Track Your Financial Runway",
            description = "Get honest predictions of how long your money will last based on your real-time daily burn rate.",
            icon = Icons.Default.Timeline,
            iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        OnboardingPageData(
            title = "Due Bills & Commitments",
            description = "Never get caught off-guard by PayLater or monthly subscription due dates ever again.",
            icon = Icons.Default.ReceiptLong,
            iconBgColor = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.onErrorContainer
        ),
        OnboardingPageData(
            title = "Choose How You Start",
            description = "Select whether to sign in or explore immediately in Guest Mode.",
            icon = Icons.Default.LockPerson,
            iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pages.size - 1)
                            }
                        }
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                if (pageIndex == pages.size - 1) {
                    // Final Choice Page (Google Sign In vs Guest Mode)
                    FinalAuthChoiceStep(
                        isGoogleLoading = isGoogleLoading,
                        isGuestLoading = isGuestLoading,
                        onSignInClick = {
                            coroutineScope.launch {
                                isGoogleLoading = true
                                val result = authManager.signInWithGoogle()
                                isGoogleLoading = false
                                when (result) {
                                    is AuthResult.Success -> onFinishOnboarding()
                                    is AuthResult.Error -> errorMessage = result.message
                                    is AuthResult.Cancelled -> { /* No-op on user cancel */ }
                                }
                            }
                        },
                        onGuestClick = {
                            coroutineScope.launch {
                                isGuestLoading = true
                                authManager.signInAnonymously()
                                isGuestLoading = false
                                onFinishOnboarding()
                            }
                        }
                    )
                } else {
                    // Standard Slide View
                    OnboardingSlide(data = pages[pageIndex])
                }
            }

            // Bottom Navigation (Dots Indicator & Next Button)
            if (!isLastPage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicator Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(pages.size) { index ->
                            val isCurrent = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 24.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCurrent) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    // Next Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(AppShapes.ExtraLargeIncreased)
                .background(data.iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = data.iconTint,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FinalAuthChoiceStep(
    isGoogleLoading: Boolean,
    isGuestLoading: Boolean,
    onSignInClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    val anyLoading = isGoogleLoading || isGuestLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            text = "Select an account mode to proceed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // OPTION 1: SIGN IN (Cloud Persistent via Google)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign In with Google",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Recommended. All your transaction history, budgets, and bills are backed up securely to the cloud. You won't lose your data when reinstalling.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onSignInClick,
                    enabled = !anyLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Sign In with Google", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OPTION 2: GUEST MODE (Temporary Device Only)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Guest Mode",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No login required. Note: If you uninstall the app or clear data, your history will not be restored and you will start from zero.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onGuestClick,
                    enabled = !anyLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isGuestLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Continue as Guest", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
