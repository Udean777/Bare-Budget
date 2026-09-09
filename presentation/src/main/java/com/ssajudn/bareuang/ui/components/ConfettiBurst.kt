package com.ssajudn.bareuang.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var rot: Float, var rotVel: Float,
    var color: Color, var size: Float,
    var life: Float
)

@Composable
fun ConfettiBurst(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 42,
    onFinished: (() -> Unit)? = null
) {
    if (!trigger) return
    val honey = MaterialTheme.colorScheme.primaryContainer
    val green = MaterialTheme.colorScheme.secondaryContainer
    val brown = MaterialTheme.colorScheme.tertiaryContainer
    val error = MaterialTheme.colorScheme.errorContainer
    val colors = listOf(honey, green, brown, error)

    var particles by remember(trigger) {
        mutableStateOf(List(particleCount) {
            Particle(
                x = 0.5f, y = 0.3f,
                vx = Random.nextFloat() * 2f - 1f,
                vy = Random.nextFloat() * -1.2f - 0.3f,
                rot = Random.nextFloat() * 360f,
                rotVel = Random.nextFloat() * 600f - 300f,
                color = colors.random(),
                size = Random.nextFloat() * 7f + 5f,
                life = 1f
            )
        })
    }
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(trigger) {
        repeat(90) { // ~1.5s at 60fps
            delay(16)
            val gravity = 0.06f
            val drag = 0.985f
            particles = particles.map { p ->
                p.apply {
                    x += vx * 0.02f
                    y += vy * 0.02f
                    vy += gravity
                    vx *= drag
                    rot += rotVel * 0.016f
                    life -= 0.011f
                }
            }
            frame++
        }
        onFinished?.invoke()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            if (p.life <= 0) return@forEach
            val alpha = p.life.coerceIn(0f, 1f)
            val cx = p.x * w
            val cy = p.y * h
            // circle + rotated square alternating
            if ((p.size.toInt() % 2) == 0) {
                drawCircle(color = p.color.copy(alpha = alpha), radius = p.size, center = Offset(cx, cy))
            } else {
                val s = p.size
                drawRect(color = p.color.copy(alpha = alpha), topLeft = Offset(cx - s/2, cy - s/2), size = Size(s, s))
            }
        }
    }
}
