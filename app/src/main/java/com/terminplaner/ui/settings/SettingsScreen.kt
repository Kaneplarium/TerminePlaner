package com.terminplaner.ui.settings

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.navigation.Screen
import com.terminplaner.util.DndManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()
    
    val personalAddress by viewModel.personalAddress.collectAsState()
    val personalBirthday by viewModel.personalBirthday.collectAsState()

    val employerName by viewModel.employerName.collectAsState()
    val employerAddress by viewModel.employerAddress.collectAsState()
    val employerPLZ by viewModel.employerPLZ.collectAsState()
    val employerCity by viewModel.employerCity.collectAsState()
    
    val userStatus by viewModel.userStatus.collectAsState()
    val calendarVisibility by viewModel.calendarVisibility.collectAsState()
    
    // Personal Settings
    val showWeekend by viewModel.showWeekend.collectAsState()
    val showHolidays by viewModel.showHolidays.collectAsState()
    val travelTime by viewModel.travelTime.collectAsState()
    val defaultReminder by viewModel.defaultReminder.collectAsState()
    val importBirthdays by viewModel.importBirthdays.collectAsState()
    val allowInvitations by viewModel.allowInvitations.collectAsState()

    // Business Settings
    val bizRecurring by viewModel.bizRecurring.collectAsState()
    val bizConfirmations by viewModel.bizConfirmations.collectAsState()
    
    val context = LocalContext.current
    remember { DndManager(context) }

    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(userName) { mutableStateOf(userName ?: "") }
    
    var showPersonalDataDialog by remember { mutableStateOf(false) }

    var showEmployerDialog by remember { mutableStateOf(false) }
    var employerNameInput by remember(employerName) { mutableStateOf(employerName ?: "") }
    var employerAddressInput by remember(employerAddress) { mutableStateOf(employerAddress ?: "") }
    var employerPLZInput by remember(employerPLZ) { mutableStateOf(employerPLZ ?: "") }
    var employerCityInput by remember(employerCity) { mutableStateOf(employerCity ?: "") }
    
    var showColorDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.filesDir, "profile_picture.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.setProfilePictureUri(file.absolutePath)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setImportBirthdays(true)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                viewModel.importData(reader.readText())
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val backgroundColor = MaterialTheme.colorScheme.background
    val isBusiness = userStatus == ThemePreferences.STATUS_BUSINESS

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                areaName = "Einstellungen",
                userStatus = userStatus,
                navController = navController,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- SECTION 1: PROFIL ---
            SettingsSection(title = "Benutzerprofil") {
                SettingsGroup {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNameDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable { photoPickerLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val bitmap = remember(profilePictureUri) {
                                    profilePictureUri?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = (userName?.take(1) ?: "U").uppercase(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName ?: "Benutzername festlegen",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isBusiness) "Business Account" else "Persönlicher Account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)

                    // Personal Account Details (Address & Birthday)
                    if (!isBusiness) {
                        SettingsItem(
                            icon = Icons.Default.ContactPage,
                            iconContainerColor = MaterialTheme.colorScheme.secondary,
                            label = "Persönliche Daten",
                            subtext = if (personalAddress.isNullOrBlank() || personalBirthday == null || personalBirthday == 0L) "Bitte vervollständigen" else null,
                            value = "Bearbeiten",
                            onClick = { showPersonalDataDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    }

                    if (isBusiness) {
                        SettingsItem(
                            icon = Icons.Default.Business,
                            iconContainerColor = MaterialTheme.colorScheme.secondary,
                            label = "Arbeitgeber",
                            subtext = if (employerName.isNullOrBlank()) "Pflichtangabe erforderlich" else null,
                            value = employerName ?: "Nicht festgelegt",
                            onClick = { showEmployerDialog = true }
                        )
                    }
                }
            }

            // --- SECTION 2: KALENDER-OPTIK ---
            SettingsSection(title = "Kalender-Optik") {
                SettingsGroup {
                    val currentThemeColor = viewModel.themeColor.collectAsState().value
                    SettingsItem(
                        icon = Icons.Rounded.Palette,
                        iconContainerColor = MaterialTheme.colorScheme.secondary,
                        label = "Standardfarbe",
                        value = when(currentThemeColor) {
                            0xFFBA1A1A -> "Rot"
                            0xFF34C759 -> "Grün"
                            0xFFAF52DE -> "Lila"
                            0xFF007AFF -> "Blau"
                            else -> "Benutzerdefiniert"
                        },
                        valueColor = Color(currentThemeColor),
                        onClick = { showColorDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    SettingsSwitchItem(
                        icon = Icons.Default.CalendarViewWeek,
                        iconContainerColor = MaterialTheme.colorScheme.primary,
                        label = "Wochenende anzeigen",
                        checked = showWeekend,
                        onCheckedChange = { viewModel.setShowWeekend(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    SettingsSwitchItem(
                        icon = Icons.Default.Event,
                        iconContainerColor = MaterialTheme.colorScheme.tertiary,
                        label = "Feiertage",
                        checked = showHolidays,
                        onCheckedChange = { viewModel.setShowHolidays(it) }
                    )
                }
            }

            // --- SECTION 3: INTELLIGENTE HELFER ---
            SettingsSection(title = "Intelligente Helfer") {
                SettingsGroup {
                    if (isBusiness) {
                        SettingsSwitchItem(
                            icon = Icons.Default.DirectionsCar,
                            iconContainerColor = MaterialTheme.colorScheme.primary,
                            label = "Fahrtzeit einkalkulieren",
                            subtext = "Nutzt deinen Standort für Verkehrshinweise",
                            checked = travelTime,
                            onCheckedChange = { viewModel.setTravelTime(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    }
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconContainerColor = MaterialTheme.colorScheme.error,
                        label = "Standard-Erinnerung",
                        value = "$defaultReminder Min. vorher",
                        onClick = { showReminderDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    SettingsSwitchItem(
                        icon = Icons.Default.Cake,
                        iconContainerColor = MaterialTheme.colorScheme.secondary,
                        label = "Geburtstage",
                        subtext = "Importiert Daten aus Kontakten",
                        checked = importBirthdays,
                        onCheckedChange = { 
                            if (it) {
                                when (PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)) {
                                    PermissionChecker.PERMISSION_GRANTED -> viewModel.setImportBirthdays(true)
                                    else -> permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            } else {
                                viewModel.setImportBirthdays(false)
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    SettingsSwitchItem(
                        icon = Icons.Default.MarkEmailRead,
                        iconContainerColor = MaterialTheme.colorScheme.primary,
                        label = "Einladungen",
                        subtext = "Andere dürfen Terminvorschläge schicken",
                        checked = allowInvitations,
                        onCheckedChange = { viewModel.setAllowInvitations(it) }
                    )
                }
            }

            // --- SECTION 4: GESCHÄFTLICH (Only if Business) ---
            if (isBusiness) {
                SettingsSection(title = "Verfügbarkeit & Buchung") {
                    SettingsGroup {
                        SettingsItem(
                            icon = Icons.Default.Schedule,
                            iconContainerColor = MaterialTheme.colorScheme.primary,
                            label = "Arbeitszeiten",
                            subtext = "Lege fest, wann Termine gebucht werden können",
                            showChevron = true,
                            onClick = { /* Navigate */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        SettingsItem(
                            icon = Icons.Default.Timer,
                            iconContainerColor = MaterialTheme.colorScheme.secondary,
                            label = "Pufferzeiten",
                            value = "10 Min.",
                            showChevron = true,
                            onClick = { /* Navigate */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        SettingsItem(
                            icon = Icons.Default.Update,
                            iconContainerColor = MaterialTheme.colorScheme.tertiary,
                            label = "Buchungsvorlauf",
                            value = "Min. 24 Std.",
                            showChevron = true,
                            onClick = { /* Navigate */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        SettingsSwitchItem(
                            icon = Icons.Default.Repeat,
                            iconContainerColor = MaterialTheme.colorScheme.primary,
                            label = "Serientermine",
                            checked = bizRecurring,
                            onCheckedChange = { viewModel.setBizRecurring(it) }
                        )
                    }
                }

                SettingsSection(title = "Kundenkommunikation") {
                    SettingsGroup {
                        SettingsSwitchItem(
                            icon = Icons.Default.CheckCircle,
                            iconContainerColor = MaterialTheme.colorScheme.primary,
                            label = "Bestätigungen",
                            subtext = "Automatische E-Mails an Kunden senden",
                            checked = bizConfirmations,
                            onCheckedChange = { viewModel.setBizConfirmations(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        SettingsItem(
                            icon = Icons.Default.NotificationsActive,
                            iconContainerColor = MaterialTheme.colorScheme.error,
                            label = "Erinnerungen für Kunden",
                            value = "2 Std. vorher",
                            onClick = { /* Navigate */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        SettingsItem(
                            icon = Icons.Default.Gavel,
                            iconContainerColor = MaterialTheme.colorScheme.outline,
                            label = "Stornierungsbedingungen",
                            onClick = { /* Navigate */ }
                        )
                    }
                }

                SettingsSection(title = "Team & Ressourcen") {
                    SettingsGroup {
                        SettingsItem(
                            icon = Icons.Default.Groups,
                            iconContainerColor = MaterialTheme.colorScheme.secondary,
                            label = "Mitarbeiter verwalten",
                            showChevron = true,
                            onClick = { /* Navigate */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        SettingsItem(
                            icon = Icons.Default.MeetingRoom,
                            iconContainerColor = MaterialTheme.colorScheme.tertiary,
                            label = "Räume & Equipment",
                            subtext = "Weise Terminen Ressourcen zu",
                            onClick = { /* Navigate */ }
                        )
                    }
                }
            }

            // --- SECTION 7: FAMILIE & FREUNDE ---
            SettingsSection(title = "Familie & Freunde") {
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.People,
                        iconContainerColor = MaterialTheme.colorScheme.secondary,
                        label = "Freigabe",
                        value = if (calendarVisibility == ThemePreferences.VISIBILITY_PRIVATE) "Persönlich (Niemand)" else "Familie (Familiengruppe)",
                        onClick = { showVisibilityDialog = true }
                    )
                }
            }

            // --- SECTION 8: DATEN & SICHERHEIT ---
            SettingsSection(title = "Daten & Sicherheit") {
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        iconContainerColor = MaterialTheme.colorScheme.error,
                        label = "Papierkorb",
                        showChevron = true,
                        onClick = { navController.navigate(Screen.Trash.route) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Upload,
                        iconContainerColor = MaterialTheme.colorScheme.primary,
                        label = "Daten exportieren",
                        onClick = { viewModel.exportData(context) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Download,
                        iconContainerColor = MaterialTheme.colorScheme.secondary,
                        label = "Daten importieren",
                        onClick = { importLauncher.launch("application/json") }
                    )
                }
                Text(
                    text = "Denken Sie daran Ihre Daten zu sichern!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version 2026.04.26.20.55",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:kontakt@kaneplarium.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Terminplaner Support")
                    }
                    context.startActivity(Intent.createChooser(intent, "Email senden"))
                }) {
                    Text("Contact Support", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Dialogs
    if (showPersonalDataDialog) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        var tempAddress by remember { mutableStateOf(personalAddress ?: "") }
        var tempBirthday by remember { mutableStateOf(personalBirthday ?: 0L) }
        var showDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPersonalDataDialog = false },
            title = { Text("Persönliche Daten") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempAddress,
                        onValueChange = { tempAddress = it },
                        label = { Text("Adresse") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cake, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Geburtsdatum", style = MaterialTheme.typography.labelSmall)
                            Text(if (tempBirthday > 0) dateFormat.format(Date(tempBirthday)) else "Nicht festgelegt")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setPersonalData(tempAddress, tempBirthday)
                    showPersonalDataDialog = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPersonalDataDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (tempBirthday > 0) tempBirthday else System.currentTimeMillis())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        tempBirthday = datePickerState.selectedDateMillis ?: tempBirthday
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") } }
            ) { DatePicker(state = datePickerState) }
        }
    }

    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            title = { Text("Kalender-Sichtbarkeit") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.setCalendarVisibility(ThemePreferences.VISIBILITY_PRIVATE); showVisibilityDialog = false }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = calendarVisibility == ThemePreferences.VISIBILITY_PRIVATE, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Persönlich", fontWeight = FontWeight.Bold)
                            Text("Nur du kannst deine Termine sehen.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.setCalendarVisibility(ThemePreferences.VISIBILITY_FAMILY); showVisibilityDialog = false }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = calendarVisibility == ThemePreferences.VISIBILITY_FAMILY, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Familie", fontWeight = FontWeight.Bold)
                            Text("Für Mitglieder deiner Familiengruppe sichtbar.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showVisibilityDialog = false }) { Text("Abbrechen") } }
        )
    }

    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Standardfarbe wählen") },
            text = {
                Column {
                    listOf(
                        "Rot" to 0xFFBA1A1A,
                        "Grün" to 0xFF34C759,
                        "Lila" to 0xFFAF52DE,
                        "Blau" to 0xFF007AFF
                    ).forEach { (name, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeColor(color); showColorDialog = false }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(color)))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = name,
                                color = Color(color),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showColorDialog = false }) { Text("Abbrechen") } }
        )
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Standard-Erinnerung") },
            text = {
                Column {
                    listOf(15, 30, 60).forEach { mins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setDefaultReminder(mins); showReminderDialog = false }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = defaultReminder == mins, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text("$mins Minuten vorher")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showReminderDialog = false }) { Text("Abbrechen") } }
        )
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = employerNameInput,
                        onValueChange = { employerNameInput = it },
                        label = { Text("Name des Unternehmens") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = employerAddressInput,
                        onValueChange = { employerAddressInput = it },
                        label = { Text("Adresse") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = employerPLZInput,
                            onValueChange = { employerPLZInput = it },
                            label = { Text("PLZ") },
                            singleLine = true,
                            modifier = Modifier.weight(0.4f)
                        )
                        OutlinedTextField(
                            value = employerCityInput,
                            onValueChange = { employerCityInput = it },
                            label = { Text("Stadt") },
                            singleLine = true,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setEmployerData(
                            employerNameInput, 
                            employerAddressInput, 
                            employerPLZInput, 
                            employerCityInput
                        )
                        showEmployerDialog = false
                    },
                    enabled = employerNameInput.isNotBlank() && 
                             employerAddressInput.isNotBlank() && 
                             employerPLZInput.isNotBlank() && 
                             employerCityInput.isNotBlank()
                ) {
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
}

@Composable
fun SettingsSection(
    title: String,
    isLocked: Boolean = false,
    onLockedClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 8.dp)
                .then(if (isLocked) Modifier.clickable { onLockedClick?.invoke() } else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Gesperrt",
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }
            if (actionIcon != null && onActionClick != null) {
                IconButton(onClick = onActionClick, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun SettingsGroup(
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .then(if (!enabled) Modifier.alpha(0.6f) else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 0.dp
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconContainerColor: Color,
    label: String,
    subtext: String? = null,
    value: String? = null,
    valueColor: Color = Color.Gray,
    showChevron: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled) { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (subtext.contains("Pflichtangabe") || subtext.contains("vervollständigen")) MaterialTheme.colorScheme.error else Color.Gray
                    )
                }
            }
            
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor,
                    fontWeight = if (valueColor != Color.Gray) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (showChevron) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    iconContainerColor: Color,
    label: String,
    subtext: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
