package com.terminplaner.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.domain.model.Appointment
import com.terminplaner.ui.components.AppointmentCard
import com.terminplaner.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.GERMAN)
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalender") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.AppointmentEdit.createRoute(selectedDate = uiState.selectedDate)
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Termin")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Vorheriger Monat")
                }
                Text(
                    text = monthFormat.format(Date(uiState.currentMonth)),
                    style = MaterialTheme.typography.headlineSmall,
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Nächster Monat")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CalendarGrid(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                allAppointments = uiState.allAppointments,
                onDateSelected = { date ->
                    viewModel.selectDate(date)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Termine am ${dateFormat.format(Date(uiState.selectedDate))}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.appointments.isEmpty()) {
                Text(
                    text = "Keine Termine für diesen Tag",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.appointments) { appointment ->
                        val category = uiState.categories.find { it.id == appointment.categoryId }
                        AppointmentCard(
                            appointment = appointment,
                            categoryColor = category?.let { Color(it.color) },
                            onClick = {
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

@Composable
fun CalendarGrid(
    currentMonth: Long,
    selectedDate: Long,
    allAppointments: List<Appointment>,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = currentMonth }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply {
        timeInMillis = currentMonth
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val firstDayOfWeek = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val dayNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalSlots = daysInMonth + firstDayOfWeek
        val rows = (totalSlots + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayIndex = row * 7 + col - firstDayOfWeek + 1
                    if (dayIndex in 1..daysInMonth) {
                        val dateCal = Calendar.getInstance().apply {
                            timeInMillis = currentMonth
                            set(Calendar.DAY_OF_MONTH, dayIndex)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dateStart = dateCal.timeInMillis
                        dateCal.add(Calendar.DAY_OF_MONTH, 1)
                        val dateEnd = dateCal.timeInMillis

                        val hasAppointments = allAppointments.any { 
                            it.dateTime >= dateStart && it.dateTime < dateEnd 
                        }

                        val isDateSelected = selectedDate >= dateStart && selectedDate < dateEnd

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isDateSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { onDateSelected(dateStart) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayIndex.toString(),
                                    color = if (isDateSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isDateSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasAppointments) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDateSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(4.dp))
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
