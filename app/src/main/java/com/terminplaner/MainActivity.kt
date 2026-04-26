package com.terminplaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.components.GreetingOverlay
import com.terminplaner.ui.navigation.AppNavigation
import com.terminplaner.ui.settings.SettingsViewModel
import com.terminplaner.ui.theme.TerminplanerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeColorLong by settingsViewModel.themeColor.collectAsState()
            val darkThemeMode by settingsViewModel.darkThemeMode.collectAsState()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState()
            
            val useDarkTheme = when (darkThemeMode) {
                ThemePreferences.MODE_LIGHT -> false
                ThemePreferences.MODE_DARK -> true
                else -> isSystemInDarkTheme()
            }
            
            TerminplanerTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColor,
                primaryColor = Color(themeColorLong)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val userName by settingsViewModel.userName.collectAsState()
                    var showGreeting by remember { mutableStateOf(true) }

                    AppNavigation(intentAction = intent.action, intentShortcut = intent.getStringExtra("shortcut"))

                    if (showGreeting) {
                        GreetingOverlay(
                            userName = userName,
                            onFinished = { showGreeting = false }
                        )
                    }
                }
            }
        }
    }
}
