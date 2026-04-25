package com.terminplaner.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.terminplaner.ui.appointments.*
import com.terminplaner.ui.calendar.CalendarScreen
import com.terminplaner.ui.category.*
import com.terminplaner.ui.settings.SettingsScreen
import com.terminplaner.ui.tasks.*
import com.terminplaner.ui.trash.TrashScreen

val bottomNavItems = listOf(
    BottomNavItem.Calendar,
    BottomNavItem.Appointments,
    BottomNavItem.Tasks,
    BottomNavItem.Settings,
)

@Composable
fun AppNavigation(
    intentAction: String? = null,
    intentShortcut: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(intentAction, intentShortcut) {
        when {
            intentAction == "ACTION_QUICK_TASK" -> {
                navController.navigate("task_edit?taskId=0&appointmentId=0")
            }
            intentShortcut == "new_appointment" -> {
                navController.navigate("appointment_edit?appointmentId=0&selectedDate=0")
            }
            intentShortcut == "today_overview" -> {
                navController.navigate("calendar")
            }
        }
    }

    Scaffold(
        bottomBar = {
            val showBottomBar = currentDestination?.route in listOf("calendar", "appointments_list", "tasks_list", "settings")
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calendar",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("calendar") {
                CalendarScreen(navController = navController)
            }
            composable("appointments_list") {
                AppointmentsListScreen(navController = navController)
            }
            composable("tasks_list") {
                TasksListScreen(navController = navController)
            }
            composable("settings") {
                SettingsScreen(navController = navController)
            }
            composable(
                route = "appointment_detail/{appointmentId}",
                arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("appointmentId") ?: 0L
                AppointmentDetailScreen(
                    appointmentId = id,
                    onDismiss = { navController.popBackStack() }
                )
            }
            composable(
                route = "appointment_edit?appointmentId={appointmentId}&selectedDate={selectedDate}",
                arguments = listOf(
                    navArgument("appointmentId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("selectedDate") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) {
                AppointmentEditScreen(navController = navController)
            }
            composable("trash") {
                TrashScreen(navController = navController)
            }
            composable("categories_list") {
                CategoriesListScreen(navController = navController)
            }
            composable("features") {
                com.terminplaner.ui.settings.FeaturesScreen(navController = navController)
            }
            composable(
                route = "category_edit?categoryId={categoryId}",
                arguments = listOf(
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) {
                CategoryEditScreen(navController = navController)
            }
            composable(
                route = "task_edit?taskId={taskId}&appointmentId={appointmentId}",
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("appointmentId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) {
                TaskEditScreen(navController = navController)
            }
        }
    }
}
