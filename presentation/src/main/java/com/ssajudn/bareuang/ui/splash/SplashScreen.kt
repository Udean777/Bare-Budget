package com.ssajudn.bareuang.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onSplashFinished: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val logoScale = remember { Animatable(0.4f) }
    val logoOffsetY = remember { Animatable(30f) }
    val contentAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.7f) }
    val overallAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                1f,
                spring(dampingRatio = 0.52f, stiffness = 320f)
            )
        }
        launch {
            logoOffsetY.animateTo(
                0f,
                spring(dampingRatio = 0.65f, stiffness = 280f)
            )
        }
        launch {
            contentAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            delay(150.milliseconds)
            badgeScale.animateTo(
                1f,
                spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMediumLow)
            )
        }
        delay(1100.milliseconds)
        overallAlpha.animateTo(0f, tween(320, easing = FastOutSlowInEasing))
        onSplashFinished(viewModel.computeStartDestination())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overallAlpha.value)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to MaterialTheme.colorScheme.background,
                        0.6f to MaterialTheme.colorScheme.surfaceContainerLow,
                        1.0f to MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.18f)
                    )
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = Spacing.ScreenHorizontal)
                .offset(y = logoOffsetY.value.dp)
        ) {
            // Logo hero — floating white pebble card with soft amber shadow
            Surface(
                modifier = Modifier
                    .size(132.dp)
                    .scale(logoScale.value)
                    .alpha(contentAlpha.value)
                    .shadow(
                        elevation = 20.dp,
                        shape = MaterialTheme.shapes.extraLarge,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        ambientColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = stringResource(R.string.splash_brand),
                        modifier = Modifier
                            .size(96.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand title — clean, bold, non-redundant
            Text(
                text = stringResource(R.string.splash_brand),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Cute pill badge tagline
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .scale(badgeScale.value)
                    .alpha(contentAlpha.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✦",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.settings_footer_tagline),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Footer — warm, reassuring, offline-first signature
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.splash_runway_tracker),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_bubbly_desc),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }
    }
}
