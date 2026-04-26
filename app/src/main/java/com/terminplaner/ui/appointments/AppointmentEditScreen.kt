package com.terminplaner.ui.appointments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.components.ColorPicker
import com.terminplaner.ui.components.TimeDropdown
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
    val isBusiness = uiState.userStatus == ThemePreferences.STATUS_BUSINESS

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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                areaName = if (uiState.isEditMode) "Termin bearbeiten" else "Neuer Termin",
                userStatus = uiState.userStatus,
                navController = navController,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { photoLauncher.launch(null) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Flyer scannen")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.suggestedDateTime != null) {
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

            EditSection(title = "DETAILS") {
                EditGroup {
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
                        placeholder = { Text("Beschreibung") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = transparentTextFieldColors()
                    )
                }
            }

            EditSection(title = if (isBusiness) "STANDORT & MITARBEITER" else "ORT & PERSONEN") {
                EditGroup {
                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = { viewModel.updateLocation(it) },
                        placeholder = { Text(if (isBusiness) "Standort oder Meeting-Link" else "Treffpunkt") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = transparentTextFieldColors()
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                    OutlinedTextField(
                        value = uiState.persons,
                        onValueChange = { viewModel.updatePersons(it) },
                        placeholder = { Text(if (isBusiness) "Team & Mitarbeiter" else "Freunde & Familie") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        colors = transparentTextFieldColors(),
                        trailingIcon = {
                            if (!isBusiness) {
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
                                    Icon(Icons.Default.ContactPage, contentDescription = "Kontakte", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                }
            }

            EditSection(title = "ZEITPUNKT") {
                EditGroup {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text("Datum", modifier = Modifier.weight(1f))
                        Text(dateFormat.format(Date(uiState.dateTime)), color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                    TimeDropdown(
                        label = "Start",
                        currentTime = uiState.dateTime,
                        onTimeSelected = { viewModel.updateDateTime(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                    TimeDropdown(
                        label = "Ende",
                        currentTime = uiState.endDateTime,
                        onTimeSelected = { viewModel.updateEndDateTime(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (isBusiness) {
                EditSection(title = "KUNDE") {
                    EditGroup {
                        OutlinedTextField(
                            value = uiState.customerName,
                            onValueChange = { viewModel.updateCustomerName(it) },
                            placeholder = { Text("Kundenname") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = transparentTextFieldColors()
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                        OutlinedTextField(
                            value = uiState.customerEmail,
                            onValueChange = { viewModel.updateCustomerEmail(it) },
                            placeholder = { Text("Kunden-E-Mail") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = transparentTextFieldColors(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${uiState.customerEmail}")
                                        putExtra(Intent.EXTRA_SUBJECT, "Terminbestätigung: ${uiState.title}")
                                        putExtra(Intent.EXTRA_TEXT, "Guten Tag ${uiState.customerName},\n\nhiermit lade ich Sie zum Termin '${uiState.title}' am ${dateFormat.format(Date(uiState.dateTime))} um ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(uiState.dateTime))} ein.")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Einladung senden"))
                                }) {
                                    Icon(Icons.Default.Email, contentDescription = "Mail senden", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vom Kunden bestätigt")
                            Checkbox(
                                checked = uiState.isConfirmed,
                                onCheckedChange = { viewModel.updateConfirmation(it) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF34C759))
                            )
                        }
                    }
                }
            }

            EditSection(title = "OPTIONEN") {
                EditGroup {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fokus-Modus")
                        Switch(
                            checked = uiState.isFocusMode,
                            onCheckedChange = { viewModel.updateFocusMode(it) },
                            enabled = !isBusiness,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF34C759))
                        )
                    }
                    if (uiState.isEditMode) {
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                        TextButton(
                            onClick = { /* ViewModel delete */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(if (isBusiness) "Termin stornieren" else "Termin löschen")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.onSaveClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = if (isBusiness) uiState.isConfirmed else true
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("In Kalender eintragen")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateTime)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val calendar = Calendar.getInstance().apply { timeInMillis = uiState.dateTime }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                        }
                        viewModel.updateDateTime(newCal.timeInMillis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun EditSection(title: String, content: @Composable () -> Unit) {
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
fun EditGroup(content: @Composable ColumnScope.() -> Unit) {
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
