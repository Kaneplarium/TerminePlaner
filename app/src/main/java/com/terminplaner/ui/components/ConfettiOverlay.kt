package com.terminplaner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ConfettiOverlay(onFinished: () -> Unit) {
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = (0..1000).random().toFloat() / 1000f,
                y = -0.1f,
                color = Color(
                    red = (0..255).random() / 255f,
                    green = (0..255).random() / 255f,
                    blue = (0..255).random() / 255f,
                    alpha = 1f
                ),
                speed = (5..15).random().toFloat() / 1000f,
                size = (4..8).random().toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    LaunchedEffect(Unit) {
        delay(3000)
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val currentY = (particle.y + animProgress * particle.speed * 50) % 1.2f
            drawCircle(
                color = particle.color,
                radius = particle.size.dp.toPx(),
                center = Offset(particle.x * size.width, currentY * size.height)
            )
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val speed: Float,
    val size: Float
)
