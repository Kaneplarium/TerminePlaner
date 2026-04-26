package com.terminplaner.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terminplaner.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun GreetingOverlay(
    userName: String?,
    onFinished: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val personalBirthday by viewModel.personalBirthday.collectAsState()
    
    var isVisible by remember { mutableStateOf(true) }
    
    val greeting = remember(personalBirthday) {
        val now = Calendar.getInstance()
        val day = now.get(Calendar.DAY_OF_MONTH)
        val month = now.get(Calendar.MONTH) // 0-based
        val hour = now.get(Calendar.HOUR_OF_DAY)

        // Check Birthday
        if (personalBirthday != null && personalBirthday!! > 0) {
            val bday = Calendar.getInstance().apply { timeInMillis = personalBirthday!! }
            if (bday.get(Calendar.DAY_OF_MONTH) == day && bday.get(Calendar.MONTH) == month) {
                return@remember "Alles Gute zum Geburtstag!"
            }
        }

        // Check Christmas
        if (month == Calendar.DECEMBER && day in 24..26) {
            return@remember "Frohe Weihnachten!"
        }

        // Check Easter (Simplified for 2026: April 5)
        if (month == Calendar.APRIL && day in 4..6) {
            return@remember "Schöne Ostern!"
        }

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
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = greeting,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
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
