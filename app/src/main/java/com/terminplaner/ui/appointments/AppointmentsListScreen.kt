package com.terminplaner.ui.appointments

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anstehend", "Erledigt")

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color.Black else Color(0xFFF2F2F7)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = backgroundColor,
        topBar = {
            Column {
                AppTopBar(
                    areaName = "Termine",
                    userStatus = uiState.userStatus,
                    navController = navController,
                    scrollBehavior = scrollBehavior
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (scrollBehavior.state.collapsedFraction > 0.5f) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    } else {
                        backgroundColor
                    },
                    divider = {}
                ) {
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
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
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
                    .padding(padding)
            ) {
                groupedAppointments.forEach { (monthKey, appointments) ->
                    item {
                        Text(
                            text = dateFormat.format(Date(monthKey)).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 28.dp, top = 24.dp, bottom = 8.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    item {
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Column {
                                appointments.forEachIndexed { index, appointment ->
                                    val category = uiState.categories.find { it.id == appointment.categoryId }
                                    AppointmentCard(
                                        appointment = appointment,
                                        categoryColor = category?.let { Color(it.color) },
                                        onEdit = {
                                            navController.navigate(
                                                Screen.AppointmentEdit.createRoute(appointmentId = appointment.id)
                                            )
                                        }
                                    )
                                    if (index < appointments.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 66.dp),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
