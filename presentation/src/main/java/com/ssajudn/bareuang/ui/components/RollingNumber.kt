package com.ssajudn.bareuang.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.ssajudn.bareuang.utils.CurrencyFormatter

@Composable
fun RollingNumber(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    formatter: (Long) -> String = { CurrencyFormatter.formatRupiah(it) }
) {
    val anim = remember { Animatable(value.toFloat()) }
    LaunchedEffect(value) { anim.animateTo(value.toFloat(), tween(700, easing = FastOutSlowInEasing)) }
    Text(text = formatter(anim.value.toLong()), style = style, modifier = modifier)
}
