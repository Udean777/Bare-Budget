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
import androidx.compose.material3.CircularProgressIndicator
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
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.utils.LanguageManager
import kotlinx.coroutines.launch

@Composable
fun animateDpAsset(isCurrent: Boolean) = animateDpAsState(
    targetValue = if (isCurrent) 24.dp else 8.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "dotWidth"
)

@Composable
fun OnboardingSlide(
    data: OnboardingPageData,
    isFirstSlide: Boolean = false,
    isCurrencySlide: Boolean = false,
    isThemeSlide: Boolean = false,
    currentLang: String = "",
    onLanguageChange: (String) -> Unit = {},
    currentCurrency: com.ssajudn.bareuang.domain.model.AppCurrency = com.ssajudn.bareuang.domain.model.AppCurrency.IDR,
    onCurrencyChange: (com.ssajudn.bareuang.domain.model.AppCurrency) -> Unit = {},
    currentDarkMode: AppThemeDarkMode = AppThemeDarkMode.FollowSystem,
    onDarkModeChange: (AppThemeDarkMode) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration hero card — floating with warm shadow
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = MaterialTheme.shapes.extraLarge,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    ambientColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
                ),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            tonalElevation = 0.dp
        ) {
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = data.title,
                modifier = Modifier
                    .size(220.dp)
                    .padding(14.dp)
                    .clip(MaterialTheme.shapes.large)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.StackLg))

        // Language toggle — only on first slide
        if (isFirstSlide) {
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(bottom = Spacing.Medium)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LanguageManager.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                        val isSelected = currentLang == code
                        Surface(
                            shape = AppShapes.Pill,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            onClick = { onLanguageChange(code) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Currency toggle — only on currency slide
        if (isCurrencySlide) {
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(bottom = Spacing.Medium)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    com.ssajudn.bareuang.domain.model.AppCurrency.entries.forEach { curr ->
                        val isSelected = currentCurrency == curr
                        Surface(
                            shape = AppShapes.Pill,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            onClick = { onCurrencyChange(curr) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (curr == com.ssajudn.bareuang.domain.model.AppCurrency.IDR) "Rupiah (Rp)" else "Dollar ($)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Theme toggle — only on theme slide
        if (isThemeSlide) {
            val themeOptions = listOf(
                Triple(AppThemeDarkMode.FollowSystem, stringResource(R.string.onboarding_theme_system), Icons.Default.BrightnessAuto),
                Triple(AppThemeDarkMode.Light, stringResource(R.string.onboarding_theme_light), Icons.Default.LightMode),
                Triple(AppThemeDarkMode.Dark, stringResource(R.string.onboarding_theme_dark), Icons.Default.DarkMode)
            )
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(bottom = Spacing.Medium)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    themeOptions.forEach { (mode, label, icon) ->
                        val isSelected = currentDarkMode == mode
                        Surface(
                            shape = AppShapes.Pill,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            onClick = { onDarkModeChange(mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step indicator badge — small honey pill
        Surface(
            shape = AppShapes.Pill,
            color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.35f)
        ) {
            Text(
                text = "✦  Bareuang",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun NotificationStep(data: OnboardingPageData, isLoading: Boolean, onAllowClick: () -> Unit, onLaterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration hero card with clean floating shadow
        Surface(
            modifier = Modifier.shadow(
                elevation = 20.dp,
                shape = MaterialTheme.shapes.extraLarge,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                ambientColor = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.15f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            tonalElevation = 0.dp
        ) {
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = data.title,
                modifier = Modifier
                    .size(220.dp)
                    .padding(14.dp)
                    .clip(MaterialTheme.shapes.large)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.StackLg))

        // "Stay on track" pill badge — secondary forest green
        Surface(
            shape = AppShapes.Pill,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(com.ssajudn.bareuang.presentation.R.string.onboarding_stay_on_track),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Allow button — AppButton with honey yellow styling, NO bottomBorder (full-width context)
        // BaruangPrimaryButton adds a bottomBorder line for tactile 3D effect which looks off
        // on full-width buttons, so we use AppButton with explicit primaryContainer colors.
        AppButton(
            onClick = onAllowClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp,
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    stringResource(R.string.notif_perm_allow),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // "Maybe later" — subtle outlined surface, NOT TextButton (avoids underline ripple edge)
        Surface(
            onClick = { if (!isLoading) onLaterClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = AppShapes.Pill,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            enabled = !isLoading
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.notif_perm_later),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
