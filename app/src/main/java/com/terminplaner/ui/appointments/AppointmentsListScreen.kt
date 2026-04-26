package com.terminplaner.ui.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.components.AppointmentCard
import com.terminplaner.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsListScreen(
    navController: NavController,
    viewModel: AppointmentsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.GERMAN)
    
    var appointmentToDelete by remember { mutableStateOf<Long?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anstehend", "Erledigt")

    Scaffold(
        topBar = {
            Column {
                AppTopBar(
                    areaName = "Termine",
                    navController = navController
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AppointmentEdit.createRoute())
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Neuer Termin")
                }
            }
        }
    ) { padding ->
        val filteredAppointments = remember(uiState.appointments, selectedTab) {
            if (selectedTab == 0) {
                uiState.appointments.filter { !it.isCompleted }
            } else {
                uiState.appointments.filter { it.isCompleted }
            }
        }

        if (filteredAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Keine anstehenden Termine" else "Keine erledigten Termine",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val groupedAppointments = filteredAppointments.groupBy { appointment ->
                Calendar.getInstance().apply {
                    timeInMillis = appointment.dateTime
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedAppointments.forEach { (monthKey, appointments) ->
                    item {
                        Text(
                            text = dateFormat.format(Date(monthKey)),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(appointments, key = { it.id }) { appointment ->
                        val category = uiState.categories.find { it.id == appointment.categoryId }
                        AppointmentCard(
                            appointment = appointment,
                            categoryColor = category?.let { androidx.compose.ui.graphics.Color(it.color) },
                            onEdit = {
                                navController.navigate(
                                    Screen.AppointmentEdit.createRoute(appointmentId = appointment.id)
                                )
                            },
                            onDelete = {
                                appointmentToDelete = appointment.id
                            }
                        )
                    }
                }
            }
        }
    }

    if (appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { appointmentToDelete = null },
            title = { Text("Termin löschen") },
            text = { Text("Möchtest du diesen Termin wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        appointmentToDelete?.let { viewModel.deleteAppointment(it) }
                        appointmentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
