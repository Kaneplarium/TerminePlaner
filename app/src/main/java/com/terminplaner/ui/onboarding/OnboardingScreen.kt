package com.terminplaner.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terminplaner.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 6

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }, label = ""
                ) { targetStep ->
                    when (targetStep) {
                        0 -> WelcomeStep()
                        1 -> NameStep { viewModel.setUserName(it) }
                        2 -> FeaturesStep()
                        3 -> ColorStep(
                            onColorSelect = { viewModel.setThemeColor(it) }
                        )
                        4 -> StorageStep(onPathSelect = { viewModel.setStoragePath(it) })
                        5 -> PermissionStep()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(totalSteps) { i ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == step) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (step < (totalSteps - 1)) {
                            step++
                        } else {
                            viewModel.completeOnboarding()
                            onFinished()
                        }
                    },
                    shape = CircleShape,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (step < totalSteps - 1) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Weiter")
                    } else {
                        Text("Loslegen")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Willkommen bei\nTerminplaner",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Deine einfache und moderne Lösung zur Organisation von Terminen und Aufgaben.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NameStep(onNameChange: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Wie heißt du?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Wir würden dich gerne persönlich begrüßen.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                onNameChange(it)
            },
            label = { Text("Dein Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FeaturesStep() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Deine Top Features",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(32.dp))

        FeatureItem(
            icon = Icons.Default.CheckCircle,
            title = "Intelligente Planung",
            description = "Warnung bei Terminüberschneidungen und automatisches Speichern deiner Eingaben."
        )
        Spacer(Modifier.height(16.dp))
        FeatureItem(
            icon = Icons.Default.Notifications,
            title = "Aufgaben & Erinnerungen",
            description = "Verwalte Aufgaben mit exakten Erinnerungen und praktischer Schlummer-Funktion."
        )
        Spacer(Modifier.height(16.dp))
        FeatureItem(
            icon = Icons.Default.Security,
            title = "Automatischer Schutz",
            description = "Deine Daten werden bei jeder Änderung automatisch im Hintergrund gesichert."
        )
        Spacer(Modifier.height(16.dp))
        FeatureItem(
            icon = Icons.Default.Palette,
            title = "Modernes Design",
            description = "Wähle zwischen Hell- und Dunkelmodus sowie individuellen Akzentfarben."
        )
        Spacer(Modifier.height(16.dp))
        FeatureItem(
            icon = Icons.Default.Favorite,
            title = "Open Source & Werbefrei",
            description = "Diese App ist 100% kostenlos, quelloffen und enthält keine nervige Werbung."
        )
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ColorStep(onColorSelect: (Long) -> Unit) {
    var selectedColor by remember { mutableLongStateOf(0xFFE53935) } // Default Red

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Dein Design",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        
        // Note: Dynamic Colors toggle is kept in background but not shown in UI as requested
        // It remains 'true' by default.

        Text(
            text = "Wähle eine Akzentfarbe für den Fall, dass keine Systemfarben verfügbar sind:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val colors = listOf(Red, Yellow, Green, Blue, Pink)
            colors.forEach { color ->
                val colorLong = color.toArgb().toLong()
                val isSelected = selectedColor == colorLong
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { 
                            selectedColor = colorLong
                            onColorSelect(colorLong)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StorageStep(onPathSelect: (String) -> Unit) {
    var selectedPath by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val path = it.toString()
            selectedPath = path
            onPathSelect(path)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.Storage, 
            contentDescription = null, 
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Speicherort & Sicherheit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Wähle einen Ordner für automatische Backups deiner Daten. \n\nHinweis: Diese App hat KEINE Cloud-Anbindung. Du bist selbst für die Sicherung deiner Daten verantwortlich!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(32.dp))
        
        Button(onClick = { launcher.launch(null) }) {
            Text(if (selectedPath == null) "Ordner wählen" else "Ordner ändern")
        }
        
        if (selectedPath != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ausgewählt: $selectedPath",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PermissionStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null, 
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Fast fertig!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Erlaube Benachrichtigungen, damit du nie wieder einen Termin verpasst. \n\nZudem benötigen wir Zugriff auf deine Kontakte für eine schnellere Terminerstellung.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(24.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "WICHTIG: Deine Daten werden nur lokal gespeichert. Wir haben KEINEN Zugriff auf eine Cloud. Sichere deine Backups regelmäßig!",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
