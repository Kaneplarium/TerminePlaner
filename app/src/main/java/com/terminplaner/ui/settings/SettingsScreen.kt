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
import com.terminplaner.ui.components.ConfettiOverlay
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
    val employer by viewModel.employer.collectAsState()
    val userStatus by viewModel.userStatus.collectAsState()
    val smartwatchSync by viewModel.smartwatchSync.collectAsState()
    
    val context = LocalContext.current
    val dndManager = remember { DndManager(context) }

    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(userName) { mutableStateOf(userName ?: "") }
    
    var showEmployerDialog by remember { mutableStateOf(false) }
    var employerInput by remember(employer) { mutableStateOf(employer ?: "") }
    
    var versionTapCount by remember { mutableIntStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }

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
                areaName = "Einstellungen",
                userStatus = userStatus,
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

            if (userStatus == ThemePreferences.STATUS_BUSINESS) {
                ListItem(
                    headlineContent = { Text("Arbeitgeber") },
                    supportingContent = { Text(employer ?: "Nicht festgelegt") },
                    leadingContent = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.clickable { showEmployerDialog = true }
                )
            }

            HorizontalDivider()

            Text(
                text = "Hilfe & Papierkorb",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            ListItem(
                headlineContent = { Text("Funktionsübersicht") },
                supportingContent = { Text("Alle Features im Überblick") },
                leadingContent = { Icon(Icons.Default.List, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate(Screen.Features.route) }
            )

            ListItem(
                headlineContent = { Text("Papierkorb") },
                supportingContent = { Text("Gelöschte Termine & Einstellungen") },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate(Screen.Trash.route) }
            )

            if (userStatus == ThemePreferences.STATUS_PRO) {
                ListItem(
                    headlineContent = { Text("App weiterempfehlen") },
                    supportingContent = { Text("Teile die App mit deinen Freunden") },
                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                    modifier = Modifier.clickable {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Schau dir diese tolle Terminplaner App an!")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                )
            }

            HorizontalDivider()

            Text(
                text = "Daten",
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
                headlineContent = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Daten exportieren")
                        Spacer(Modifier.width(8.dp))
                        var showStorageTip by remember { mutableStateOf(false) }
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Hinweis",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showStorageTip = true }
                        )
                        if (showStorageTip) {
                            AlertDialog(
                                onDismissRequest = { showStorageTip = false },
                                title = { Text("Sicherheitshinweis") },
                                text = { Text("Denken Sie daran Ihre Daten regelmäßig zu sichern!") },
                                confirmButton = {
                                    TextButton(onClick = { showStorageTip = false }) { Text("OK") }
                                }
                            )
                        }
                    }
                },
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
                text = "Integrationen",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            ListItem(
                headlineContent = { Text("Smartwatch Verknüpfung") },
                supportingContent = { Text("Termine automatisch auf die Uhr übertragen") },
                leadingContent = { Icon(Icons.Default.Watch, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = smartwatchSync,
                        onCheckedChange = { viewModel.setSmartwatchSync(it) }
                    )
                }
            )

            HorizontalDivider()

            Text(
                text = "Support & Design",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
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

            if (userStatus != ThemePreferences.STATUS_BUSINESS && !dndManager.hasPermission()) {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version 2026.04.26.20.55",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        versionTapCount++
                        when (versionTapCount) {
                            3 -> {
                                if (userStatus != ThemePreferences.STATUS_PRO) {
                                    showConfetti = true
                                    viewModel.setUserStatus(ThemePreferences.STATUS_PRO)
                                }
                            }
                            5 -> {
                                if (userStatus != ThemePreferences.STATUS_BUSINESS) {
                                    showConfetti = true
                                    viewModel.setUserStatus(ThemePreferences.STATUS_BUSINESS)
                                }
                                versionTapCount = 0
                            }
                        }
                    }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Contact",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:kontakt@kaneplarium.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Terminplaner Feedback")
                        }
                        context.startActivity(Intent.createChooser(intent, "Email senden"))
                    }
                )
            }
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

    if (showEmployerDialog) {
        AlertDialog(
            onDismissRequest = { showEmployerDialog = false },
            title = { Text("Arbeitgeber festlegen") },
            text = {
                OutlinedTextField(
                    value = employerInput,
                    onValueChange = { employerInput = it },
                    label = { Text("Name des Unternehmens") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setEmployer(employerInput)
                    showEmployerDialog = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmployerDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (showConfetti) {
        ConfettiOverlay(onFinished = { showConfetti = false })
    }
}
