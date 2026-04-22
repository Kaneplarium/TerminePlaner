package com.terminplaner.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.ui.navigation.Screen
import com.terminplaner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val context = LocalContext.current

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
            TopAppBar(
                title = { Text("Einstellungen") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Papierkorb") },
                supportingContent = { Text("Gelöschte Termine anzeigen") },
                leadingContent = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Trash.route)
                }
            )

            Divider()

            ListItem(
                headlineContent = { Text("Kategorien verwalten") },
                supportingContent = { Text("Kategorien erstellen und bearbeiten") },
                leadingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    navController.navigate(Screen.CategoriesList.route)
                }
            )

            Divider()

            ListItem(
                headlineContent = { Text("Daten exportieren") },
                supportingContent = { Text("Termine und Kategorien als JSON speichern") },
                leadingContent = {
                    Icon(Icons.Default.Upload, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    viewModel.exportData(context)
                }
            )

            Divider()

            ListItem(
                headlineContent = { Text("Daten importieren") },
                supportingContent = { Text("Termine und Kategorien aus JSON laden") },
                leadingContent = {
                    Icon(Icons.Default.Upload, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    importLauncher.launch("application/json")
                }
            )

            Divider()

            Text(
                text = "Designfarbe",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val colors = listOf(Blue, Yellow, Red, Green, Purple)
                colors.forEach { color ->
                    val isSelected = themeColor == color.toArgb().toLong()
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { viewModel.setThemeColor(color.toArgb().toLong()) },
                        shape = MaterialTheme.shapes.small,
                        color = color,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
                        } else null
                    ) {}
                }
            }

            if (uiState.exportSuccess) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Export erfolgreich")
                }
            }

            if (uiState.importSuccess) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Import erfolgreich")
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }
}