package com.ssajudn.bareuang.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.utils.LanguageManager
import kotlinx.coroutines.launch


data class OnboardingPageData(
    val title: String,
    val description: String,
    val imageRes: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isStarting by remember { mutableStateOf(false) }
    val currentDarkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.currency.collectAsStateWithLifecycle()

    val pages = listOf(
        OnboardingPageData(
            stringResource(R.string.onboarding_slide1_title),
            stringResource(R.string.onboarding_slide1_desc),
            R.drawable.img_onboarding_expense
        ),
        OnboardingPageData(
            stringResource(R.string.onboarding_slide2_title),
            stringResource(R.string.onboarding_slide2_desc),
            R.drawable.img_onboarding_runway
        ),
        OnboardingPageData(
            stringResource(R.string.onboarding_slide3_title),
            stringResource(R.string.onboarding_slide3_desc),
            R.drawable.img_onboarding_bills
        ),
        OnboardingPageData(
            stringResource(R.string.onboarding_currency_title),
            stringResource(R.string.onboarding_currency_desc),
            R.drawable.img_onboarding_currency
        ),
        OnboardingPageData(
            stringResource(R.string.onboarding_theme_title),
            stringResource(R.string.onboarding_theme_desc),
            R.drawable.img_onboarding_theme
        ),
        OnboardingPageData(
            stringResource(R.string.notif_perm_title),
            stringResource(R.string.notif_perm_desc),
            R.drawable.img_onboarding_notif
        )
    )

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        isStarting = true
        viewModel.startLocalSession { onFinishOnboarding() }
    }

    var currentLang by remember { mutableStateOf(LanguageManager.getCurrentLanguageCode(context)) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    // Warm cream-to-amber gradient background — Bareuang brand §6 Tonal Layering
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to MaterialTheme.colorScheme.background,
                        0.6f to MaterialTheme.colorScheme.surfaceContainerLow,
                        1.0f to MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.18f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = Spacing.ScreenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar: logo + skip ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini logo pill — brand anchor
                Surface(
                    shape = AppShapes.Pill,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                        Text(
                            text = "Bareuang",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                if (!isLastPage) {
                    AppTextButton(
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pages.size - 1) } }
                    ) {
                        Text(
                            stringResource(R.string.tour_skip),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Pager ─────────────────────────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                if (pageIndex == pages.size - 1) {
                    NotificationStep(
                        data = pages[pageIndex],
                        isLoading = isStarting,
                        onAllowClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            else {
                                isStarting =
                                    true; viewModel.startLocalSession { onFinishOnboarding() }
                            }
                        },
                        onLaterClick = {
                            isStarting = true; viewModel.startLocalSession { onFinishOnboarding() }
                        }
                    )
                } else {
                    OnboardingSlide(
                        data = pages[pageIndex],
                        isFirstSlide = pageIndex == 0,
                        isCurrencySlide = pageIndex == 3,
                        isThemeSlide = pageIndex == 4,
                        currentLang = currentLang,
                        onLanguageChange = { code ->
                            currentLang = code
                            LanguageManager.setLanguage(context, code)
                        },
                        currentCurrency = currentCurrency,
                        onCurrencyChange = { curr ->
                            viewModel.setCurrency(curr)
                        },
                        currentDarkMode = currentDarkMode,
                        onDarkModeChange = { mode ->
                            viewModel.setDarkMode(mode)
                        }
                    )
                }
            }

            // ── Bottom controls (dots + next) — hidden on last page ───────────────
            if (!isLastPage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated pill dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size - 1) { index ->
                            val isCurrent = pagerState.currentPage == index
                            val dotWidth by animateDpAsset(isCurrent)
                            val dotColor by animateColorAsState(
                                targetValue = if (isCurrent)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "dotColor"
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = dotWidth, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    // Next CTA — honey yellow pill, no bottomBorder line
                    AppButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        shape = AppShapes.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 0.dp,
                        ),
                    ) {
                        Text(
                            stringResource(R.string.tour_next),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
