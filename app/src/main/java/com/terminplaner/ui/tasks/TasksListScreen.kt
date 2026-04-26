package com.terminplaner.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.domain.model.Task
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    navController: NavController,
    viewModel: TasksListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anstehend", "Erledigt")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEditSheet by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableLongStateOf(0L) }

    Scaffold(
        topBar = {
            Column {
                AppTopBar(
                    areaName = "Aufgaben",
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
                        editingTaskId = 0L
                        showEditSheet = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Neue Aufgabe")
                }
            }
        }
    ) { padding ->
        val filteredTasks = remember(uiState.tasks, selectedTab) {
            if (selectedTab == 0) {
                uiState.tasks.filter { !it.isCompleted }
            } else {
                uiState.tasks.filter { it.isCompleted }
            }
        }

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Keine anstehenden Aufgaben" else "Keine erledigten Aufgaben",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    val associatedAppointment = uiState.appointments.find { it.id == task.appointmentId }
                    
                    TaskItem(
                        task = task,
                        appointmentTitle = associatedAppointment?.title,
                        appointmentDateTime = associatedAppointment?.dateTime,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task.id) },
                        onClick = {
                            editingTaskId = task.id
                            showEditSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val editViewModel: TaskEditViewModel = hiltViewModel(
                key = "task_edit_$editingTaskId"
            )
            
            // Explicitly load the task since we're not using navigation arguments
            LaunchedEffect(editingTaskId) {
                if (editingTaskId > 0) {
                    editViewModel.loadTask(editingTaskId)
                }
            }
            
            TaskEditContent(
                viewModel = editViewModel,
                onSaved = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showEditSheet = false
                    }
                },
                onCancel = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showEditSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    appointmentTitle: String?,
    appointmentDateTime: Long?,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (appointmentTitle != null) {
                    Text(
                        text = "Termin: $appointmentTitle (${dateFormat.format(Date(appointmentDateTime ?: 0))})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
