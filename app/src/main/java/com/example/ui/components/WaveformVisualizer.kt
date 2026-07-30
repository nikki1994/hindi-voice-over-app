package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            animationProgress.snapTo(0f)
        }
    }

    val barHeights = remember {
        List(barCount) { Random.nextFloat().coerceIn(0.2f, 0.95f) }
    }

    Box(modifier = modifier.fillMaxWidth().height(48.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(6f)
            val spacing = barWidth * 0.5f

            val totalWidth = (barWidth + spacing) * barCount
            val startX = (width - totalWidth) / 2f

            for (i in 0 until barCount) {
                val x = startX + i * (barWidth + spacing)
                val baseHeight = barHeights[i] * height

                val animatedHeightFactor = if (isPlaying) {
                    val phase = (i.toFloat() / barCount + animationProgress.value) * Math.PI.toFloat() * 2f
                    0.3f + 0.7f * ((Math.sin(phase.toDouble()).toFloat() + 1f) / 2f)
                } else {
                    0.25f
                }

                val currentBarHeight = (baseHeight * animatedHeightFactor).coerceAtLeast(8f)
                val topY = (height - currentBarHeight) / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, secondaryColor, tertiaryColor)
                    ),
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, currentBarHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
