package com.terminplaner.ui.appointments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.components.ColorPicker
import com.terminplaner.ui.components.TimeDropdown
import com.terminplaner.util.ExternalCalendarHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentEditScreen(
    navController: NavController,
    viewModel: AppointmentEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    var showDatePicker by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.processFlyer(bitmap)
        }
    }

    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
            val cursor = context.contentResolver.query(contactUri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    val currentPersons = uiState.persons
                    val newPersons = if (currentPersons.isBlank()) name else "$currentPersons, $name"
                    viewModel.updatePersons(newPersons)
                }
                cursor.close()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            contactLauncher.launch(intent)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                areaName = if (uiState.isEditMode) "Termin bearbeiten" else "Neuer Termin",
                navController = navController,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { photoLauncher.launch(null) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Flyer scannen")
                    }
                    IconButton(onClick = { viewModel.onSaveClick() }) {
                        Icon(Icons.Default.Check, contentDescription = "Fertig")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.suggestedDateTime != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Datum erkannt: ${dateTimeFormat.format(Date(uiState.suggestedDateTime!!))}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.applySuggestion() }) {
                            Text("Anwenden")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.titleError,
                supportingText = if (uiState.titleError) {
                    { Text("Titel ist erforderlich") }
                } else null
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text("Veranstaltungsort") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
            )

            OutlinedTextField(
                value = uiState.persons,
                onValueChange = { viewModel.updatePersons(it) },
                label = { Text("Personen") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                                contactLauncher.launch(intent)
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }) {
                        Icon(Icons.Default.ContactPage, contentDescription = "Kontakte")
                    }
                }
            )

            OutlinedTextField(
                value = dateFormat.format(Date(uiState.dateTime)),
                onValueChange = { },
                label = { Text("Datum") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = uiState.isPastDateError,
                supportingText = if (uiState.isPastDateError) {
                    { Text("Termine in der Vergangenheit sind nicht erlaubt") }
                } else null,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Datum wählen")
                    }
                }
            )

            Text("Uhrzeit", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeDropdown(
                    label = "Start",
                    currentTime = uiState.dateTime,
                    onTimeSelected = { viewModel.updateDateTime(it) },
                    modifier = Modifier.weight(1f)
                )

                TimeDropdown(
                    label = "Ende",
                    currentTime = uiState.endDateTime,
                    onTimeSelected = { viewModel.updateEndDateTime(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.hasOverlap) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Achtung: Zeitkonflikt mit anderem Termin!",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (uiState.categories.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.categories.find { it.id == uiState.categoryId }?.name ?: "Keine Kategorie",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Kategorie") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Keine Kategorie") },
                            onClick = {
                                viewModel.updateCategory(null)
                                expanded = false
                            }
                        )
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.updateCategory(category.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            ColorPicker(
                selectedColor = uiState.color ?: 0xFFE53935.toInt(),
                onColorSelected = { viewModel.updateColor(it) }
            )

            OutlinedButton(
                onClick = { viewModel.updateFocusMode(!uiState.isFocusMode) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (uiState.isFocusMode) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        contentColor = Color(0xFF2E7D32)
                    )
                } else ButtonDefaults.outlinedButtonColors(),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (uiState.isFocusMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(
                    imageVector = if (uiState.isFocusMode) Icons.Default.DoNotDisturbOn else Icons.Default.DoNotDisturbOff,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.isFocusMode) "Fokus-Modus aktiv" else "Fokus-Modus deaktiviert")
            }

            OutlinedButton(
                onClick = {
                    val appointment = com.terminplaner.domain.model.Appointment(
                        title = uiState.title,
                        description = uiState.description,
                        location = uiState.location,
                        persons = uiState.persons,
                        dateTime = uiState.dateTime,
                        endDateTime = uiState.endDateTime
                    )
                    ExternalCalendarHelper.addToExternalCalendar(context, appointment)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("In Kalender eintragen")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.showOverlapDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOverlapDialog() },
            title = { Text("Termin-Konflikt") },
            text = { Text("Zu dieser Zeit gibt es bereits einen anderen Termin. Möchtest du die aktuelle Zeit beibehalten oder ändern?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmOverlapSave() }) {
                    Text("Beibehalten")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOverlapDialog() }) {
                    Text("Ändern")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateTime
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = uiState.dateTime
                        }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        }
                        viewModel.updateDateTime(newCal.timeInMillis)
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
