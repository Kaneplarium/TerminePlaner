package com.terminplaner.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terminplaner.ui.components.TimeDropdown
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditContent(
    viewModel: TaskEditViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header with Save button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Abbrechen")
            }
            Text(
                text = if (uiState.isEditMode) "Aufgabe bearbeiten" else "Neue Aufgabe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { viewModel.save() }) {
                Text("Speichern")
            }
        }

        if (uiState.suggestedReminderTime != null) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Datum erkannt: ${dateFormat.format(Date(uiState.suggestedReminderTime!!))}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.applySuggestion() }) {
                        Text("Anwenden")
                    }
                }
            }
        }

        TaskEditSection(title = "DETAILS") {
            TaskEditGroup {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = { Text("Titel") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.titleError,
                    colors = transparentTextFieldColors()
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("Beschreibung (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = transparentTextFieldColors()
                )
            }
        }

        TaskEditSection(title = "TYP & PRIORITÄT") {
            TaskEditGroup {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Business Aufgabe")
                    }
                    Switch(
                        checked = uiState.isBusiness,
                        onCheckedChange = { viewModel.updateBusiness(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF34C759))
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text("Wichtig")
                    }
                    Switch(
                        checked = uiState.isImportant,
                        onCheckedChange = { viewModel.updateImportant(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF34C759))
                    )
                }
            }
        }

        TaskEditSection(title = "ERINNERUNG") {
            TaskEditGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Datum", modifier = Modifier.weight(1f))
                    Text(
                        uiState.reminderTime?.let { fullDateFormat.format(Date(it)) } ?: "Keine",
                        color = if (uiState.reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (uiState.reminderTime != null) {
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                    TimeDropdown(
                        label = "Uhrzeit",
                        currentTime = uiState.reminderTime ?: System.currentTimeMillis(),
                        onTimeSelected = { viewModel.updateReminderTime(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                    TextButton(
                        onClick = { viewModel.updateReminderTime(null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Erinnerung entfernen")
                    }
                }
            }
        }

        TaskEditSection(title = "ZUORDNUNG") {
            TaskEditGroup {
                val selectedAppointment = uiState.appointments.find { it.id == uiState.appointmentId }
                var expanded by remember { mutableStateOf(false) }
                
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Text("Termin", modifier = Modifier.weight(1f))
                        Text(
                            selectedAppointment?.title ?: "Kein",
                            color = if (selectedAppointment != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Kein Termin") },
                            onClick = {
                                viewModel.updateAppointment(null)
                                expanded = false
                            }
                        )
                        uiState.appointments.forEach { appointment ->
                            DropdownMenuItem(
                                text = { Text(appointment.title) },
                                onClick = {
                                    viewModel.updateAppointment(appointment.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.reminderTime ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val currentReminder = uiState.reminderTime ?: System.currentTimeMillis()
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = currentReminder
                        }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        }
                        viewModel.updateReminderTime(newCal.timeInMillis)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun TaskEditSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun TaskEditGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 0.dp
    ) {
        Column(content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun transparentTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
