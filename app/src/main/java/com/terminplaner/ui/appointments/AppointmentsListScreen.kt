package com.terminplaner.ui.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Termine") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AppointmentEdit.createRoute())
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Termin")
            }
        }
    ) { padding ->
        if (uiState.appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Keine Termine vorhanden",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val groupedAppointments = uiState.appointments.groupBy { appointment ->
                Calendar.getInstance().apply {
                    timeInMillis = appointment.dateTime
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
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
                                viewModel.deleteAppointment(appointment.id)
                            }
                        )
                    }
                }
            }
        }
    }
}