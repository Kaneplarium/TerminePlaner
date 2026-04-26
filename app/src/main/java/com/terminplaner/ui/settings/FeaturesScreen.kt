package com.terminplaner.ui.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userStatus by viewModel.userStatus.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color.Black else Color(0xFFF2F2F7)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                areaName = "Funktionsübersicht",
                userStatus = userStatus,
                navController = navController,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Was bietet Terminplaner?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Intelligente Planung",
                        description = "Erkenne Terminüberschneidungen automatisch und plane deinen Tag effizient."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    FeatureItem(
                        icon = Icons.Default.Notifications,
                        title = "Genaue Erinnerungen",
                        description = "Verpasse nie wieder einen Termin mit präzisen Benachrichtigungen für deine Aufgaben."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    FeatureItem(
                        icon = Icons.Default.Security,
                        title = "Datenschutz & Backups",
                        description = "Deine Daten sind sicher. Nutze die automatische Sicherung und Hintergrund-Backups."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    FeatureItem(
                        icon = Icons.Default.Code,
                        title = "Open Source",
                        description = "Transparenz ist uns wichtig. Der Quellcode ist offen und für jeden einsehbar."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    FeatureItem(
                        icon = Icons.Default.Block,
                        title = "Werbefrei & Kostenlos",
                        description = "Keine versteckten Kosten, keine störende Werbung. 100% Fokus auf deine Termine."
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
