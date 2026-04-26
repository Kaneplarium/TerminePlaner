package com.terminplaner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Calendar : Screen("calendar")
    data object AppointmentsList : Screen("appointments_list")
    data object TasksList : Screen("tasks_list")
    data object Settings : Screen("settings")
    data object AppointmentEdit : Screen("appointment_edit?appointmentId={appointmentId}&selectedDate={selectedDate}") {
        fun createRoute(appointmentId: Long? = null, selectedDate: Long? = null): String {
            return "appointment_edit?appointmentId=${appointmentId ?: 0}&selectedDate=${selectedDate ?: 0}"
        }
    }
    data object Trash : Screen("trash")
    data object CategoriesList : Screen("categories_list")
    data object Features : Screen("features")
    data object CategoryEdit : Screen("category_edit?categoryId={categoryId}") {
        fun createRoute(categoryId: Long? = null): String {
            return "category_edit?categoryId=${categoryId ?: 0}"
        }
    }
    data object TaskEdit : Screen("task_edit?taskId={taskId}&appointmentId={appointmentId}") {
        fun createRoute(taskId: Long? = null, appointmentId: Long? = null): String {
            return "task_edit?taskId=${taskId ?: 0}&appointmentId=${appointmentId ?: 0}"
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Today : BottomNavItem("calendar", "Heute", Icons.Default.CalendarToday)
    data object Tasks : BottomNavItem("tasks_list", "Aufgaben", Icons.Default.FormatListBulleted)
    data object Settings : BottomNavItem("settings", "Einstellungen", Icons.Default.Settings)
}
