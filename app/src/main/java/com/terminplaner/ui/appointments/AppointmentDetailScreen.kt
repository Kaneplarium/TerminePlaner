package com.terminplaner.ui.appointments

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terminplaner.data.preferences.ThemePreferences
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: Long,
    onDismiss: () -> Unit,
    viewModel: AppointmentsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appointment = uiState.appointments.find { it.id == appointmentId } ?: return
    val isBusiness = uiState.userStatus == ThemePreferences.STATUS_BUSINESS

    val dateFormat = SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMAN)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    var offsetY by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        if (delta > 0 || offsetY > 0) {
            offsetY += delta
        }
    }

    LaunchedEffect(offsetY) {
        if (offsetY > 400f) {
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = {
                    if (offsetY <= 400f) offsetY = 0f
                }
            ),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Schließen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appointment.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            if (appointment.isConfirmed) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF34C759).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34C759))
                ) {
                    Text(
                        text = "BESTÄTIGT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailItem(
                icon = Icons.Default.LocationOn,
                label = if (isBusiness) "STANDORT / LINK" else "TREFFPUNKT",
                text = appointment.location ?: "Nicht festgelegt"
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            DetailItem(
                icon = Icons.Default.People,
                label = if (isBusiness) "TEAM & MITARBEITER" else "FREUNDE & FAMILIE",
                text = appointment.persons ?: "Keine hinzugefügt"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ZEITPUNKT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dateFormat.format(Date(appointment.dateTime)),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${timeFormat.format(Date(appointment.dateTime))} - ${timeFormat.format(Date(appointment.endDateTime))}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!appointment.description.isNullOrBlank()) {
                Text(
                    text = "BESCHREIBUNG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = appointment.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, text: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
