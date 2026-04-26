package com.terminplaner

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.components.GreetingOverlay
import com.terminplaner.ui.navigation.AppNavigation
import com.terminplaner.ui.settings.SettingsViewModel
import com.terminplaner.ui.theme.TerminplanerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeColorLong by settingsViewModel.themeColor.collectAsState()
            val darkThemeMode by settingsViewModel.darkThemeMode.collectAsState()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState()
            
            var showPermissionDialog by remember { mutableStateOf(false) }
            val isFirstRun by settingsViewModel.isFirstRunCompleted.collectAsState()

            LaunchedEffect(isFirstRun) {
                if (!isFirstRun) {
                    showPermissionDialog = true
                }
            }

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

                    if (showPermissionDialog) {
                        PermissionRequestDialog(
                            onDismiss = { showPermissionDialog = false },
                            onRequest = {
                                val permissions = mutableListOf(
                                    Manifest.permission.READ_CONTACTS
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(permissions.toTypedArray())
                                showPermissionDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestDialog(
    onDismiss: () -> Unit,
    onRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Berechtigungen benötigt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Um alle Funktionen nutzen zu können, benötigen wir folgende Berechtigungen:")
                
                PermissionItem("Kontakte", "Terminerstellung und kontaktauswahl")
                PermissionItem("Benachrichtigungen", "um benachrichtigungen zu bekommen")
                PermissionItem("Dateien", "um backups zu erstellen")
                
                HorizontalDivider()
                
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Hinweis: Diese App hat keinen Zugriff auf eine Cloud. Du musst deine Daten selbst über die Backup-Funktion sichern!",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onRequest) {
                Text("Berechtigungen anfragen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Später")
            }
        }
    )
}

@Composable
fun PermissionItem(title: String, reason: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(text = reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
