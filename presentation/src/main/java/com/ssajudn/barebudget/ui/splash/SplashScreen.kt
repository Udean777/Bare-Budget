package com.ssajudn.barebudget.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onSplashFinished: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {

    val scale = remember { Animatable(0.6f) }
    val overallAlpha = remember { Animatable(1f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo scale and alpha in parallel
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.65f,
                stiffness = 300f
            )
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // Hold for brief branded impression
        delay(700.milliseconds)

        // Smoothly fade out entire splash screen before transitioning
        overallAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )

        // Determine destination
        val destination = viewModel.computeStartDestination()

        onSplashFinished(destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overallAlpha.value)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official BareBudget Logo
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "BareBudget Logo",
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clip(MaterialTheme.shapes.extraLarge)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "Bare Budget",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Frictionless Personal Finance",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Bottom Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Survival Runway Tracker",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
