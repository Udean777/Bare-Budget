package com.ssajudn.bareuang.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun StaggeredFadeIn(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay((index * 40).toLong()); visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260, delayMillis = 0)) + slideInVertically(tween(300)) { it / 4 },
        modifier = modifier
    ) { content() }
}
