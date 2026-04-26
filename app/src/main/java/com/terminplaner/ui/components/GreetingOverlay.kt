package com.terminplaner.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun GreetingOverlay(
    userName: String?,
    onFinished: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }
    
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Guten Morgen"
            in 12..17 -> "Guten Tag"
            in 18..22 -> "Guten Abend"
            else -> "Gute Nacht"
        }
    }

    LaunchedEffect(Unit) {
        delay(2500)
        isVisible = false
        delay(500)
        onFinished()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            BokehBackground()
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = greeting,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light
                )
                if (!userName.isNullOrBlank()) {
                    Text(
                        text = userName,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BokehBackground() {
    val circles = remember {
        List(15) {
            Triple(
                Offset((0..1000).random().toFloat() / 1000f, (0..1000).random().toFloat() / 1000f),
                (50..150).random().dp,
                Color(
                    red = (150..255).random() / 255f,
                    green = (100..200).random() / 255f,
                    blue = (200..255).random() / 255f,
                    alpha = 0.3f
                )
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        circles.forEach { (offset, radius, color) ->
            drawCircle(
                color = color,
                radius = radius.toPx(),
                center = Offset(offset.x * size.width, offset.y * size.height)
            )
        }
    }
}
