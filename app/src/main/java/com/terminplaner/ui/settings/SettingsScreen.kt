package com.terminplaner.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.navigation.Screen
import com.terminplaner.ui.theme.*
import com.terminplaner.util.DndManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val darkThemeMode by viewModel.darkThemeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val context = LocalContext.current
    val dndManager = remember { DndManager(context) }

    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(userName) { mutableStateOf(userName ?: "") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                viewModel.importData(reader.readText())
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Einstellungen",
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text("Dein Name") },
                supportingContent = { Text(userName ?: "Nicht festgelegt") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.clickable { showNameDialog = true }
            )

            HorizontalDivider()

            Text(
                text = "Papierkorb",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            ListItem(
                headlineContent = { Text("Papierkorb") },
                supportingContent = { Text("Gelöschte Termine & Einstellungen") },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate(Screen.Trash.route) }
            )

            HorizontalDivider()

            Text(
                text = "App-Verwaltung & Daten",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            ListItem(
                headlineContent = { Text("Kategorien verwalten") },
                supportingContent = { Text("Kategorien erstellen und bearbeiten") },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate(Screen.CategoriesList.route) }
            )

            ListItem(
                headlineContent = { Text("Funktionsübersicht") },
                supportingContent = { Text("Alle Features im Überblick") },
                leadingContent = { Icon(Icons.Default.List, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate(Screen.Features.route) }
            )

            ListItem(
                headlineContent = { Text("Daten exportieren") },
                supportingContent = { Text("Termine und Kategorien speichern") },
                leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                modifier = Modifier.clickable { viewModel.exportData(context) }
            )

            ListItem(
                headlineContent = { Text("Daten importieren") },
                supportingContent = { Text("Daten aus JSON laden") },
                leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                modifier = Modifier.clickable { importLauncher.launch("application/json") }
            )

            HorizontalDivider()

            Text(
                text = "Support & Design",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            ListItem(
                headlineContent = { Text("Entwickler kontaktieren") },
                supportingContent = { Text("Feedback oder Fragen senden") },
                leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:kontakt@kaneplarium.com")
                        putExtra(Intent.EXTRA_SUBJECT, "TerminePlaner Feedback")
                    }
                    context.startActivity(Intent.createChooser(intent, "Email senden"))
                }
            )

            ListItem(
                headlineContent = { Text("Dunkles Design") },
                supportingContent = { 
                    Text(
                        when(darkThemeMode) {
                            ThemePreferences.MODE_LIGHT -> "Hell"
                            ThemePreferences.MODE_DARK -> "Dunkel"
                            else -> "System-Standard"
                        }
                    )
                },
                leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                trailingContent = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Wählen")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("System-Standard") },
                                onClick = { 
                                    viewModel.setDarkMode(ThemePreferences.MODE_SYSTEM)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hell") },
                                onClick = { 
                                    viewModel.setDarkMode(ThemePreferences.MODE_LIGHT)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dunkel") },
                                onClick = { 
                                    viewModel.setDarkMode(ThemePreferences.MODE_DARK)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )

            if (!dndManager.hasPermission()) {
                ListItem(
                    headlineContent = { Text("Fokus-Modus Berechtigung") },
                    supportingContent = { Text("Erforderlich für automatischen 'Bitte nicht stören' Modus") },
                    leadingContent = { Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { dndManager.requestPermission() }
                )
            }

            // Note: Dynamic Colors toggle is kept in background but not shown in UI as requested
            
            if (!dynamicColor) {
                Text(
                    text = "Designfarbe",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val colors = listOf(Red, Yellow, Green, Blue, Pink)
                    colors.forEach { color ->
                        val colorLong = color.toArgb().toLong()
                        val isSelected = themeColor == colorLong
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { viewModel.setThemeColor(colorLong) },
                            shape = CircleShape,
                            color = color,
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                            } else null
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Version 2026.04.25.00.55",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.exportSuccess) {
            Snackbar(modifier = Modifier.padding(16.dp)) { Text("Export erfolgreich") }
        }
        if (uiState.importSuccess) {
            Snackbar(modifier = Modifier.padding(16.dp)) { Text("Import erfolgreich") }
        }
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) { Text(uiState.error!!) }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Name ändern") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Dein Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setUserName(nameInput)
                    showNameDialog = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
