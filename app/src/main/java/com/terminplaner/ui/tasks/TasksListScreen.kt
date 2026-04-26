package com.terminplaner.ui.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.domain.model.Task
import com.terminplaner.ui.components.AppTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    navController: NavController,
    viewModel: TasksListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isBusinessUser = uiState.userStatus == com.terminplaner.data.preferences.ThemePreferences.STATUS_BUSINESS
    val tabs = if (isBusinessUser) listOf("Privat", "Business") else listOf("Privat")

    // Ensure selectedTabIndex is valid if tabs change
    LaunchedEffect(tabs) {
        if (selectedTabIndex >= tabs.size) {
            selectedTabIndex = 0
        }
    }

    var showEditSheet by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableLongStateOf(0L) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var completedTasksExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                areaName = "Meine Aufgaben",
                userStatus = uiState.userStatus,
                navController = navController,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            // Button removed per user request
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable { 
                    editingTaskId = 0L
                    showEditSheet = true
                }
        ) {
            // Google Tasks Style Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }

            val filteredTasks = uiState.tasks.filter { 
                if (selectedTabIndex == 0) !it.isBusiness else it.isBusiness 
            }
            val activeTasks = filteredTasks.filter { !it.isCompleted }
            val completedTasks = filteredTasks.filter { it.isCompleted }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(activeTasks, key = { it.id }) { task ->
                    TaskSwipeItem(
                        task = task,
                        onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                        onClick = {
                            editingTaskId = task.id
                            showEditSheet = true
                        },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    "Erledigt (${completedTasks.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                ) 
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = if (completedTasksExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable { completedTasksExpanded = !completedTasksExpanded }
                        )
                    }

                    if (completedTasksExpanded) {
                        items(completedTasks, key = { it.id }) { task ->
                            TaskSwipeItem(
                                task = task,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                onClick = {
                                    editingTaskId = task.id
                                    showEditSheet = true
                                },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
                
                if (activeTasks.isEmpty() && (completedTasks.isEmpty() || !completedTasksExpanded)) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.Checklist,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Alles erledigt!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState
        ) {
            val editViewModel: TaskEditViewModel = hiltViewModel(
                key = "task_edit_$editingTaskId"
            )
            
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSwipeItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    else -> MaterialTheme.colorScheme.errorContainer
                }, label = "color"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        TaskItem(
            title = task.title,
            isDone = task.isCompleted,
            onCheckedChange = onCheckedChange,
            onClick = onClick,
            onDelete = onDelete
        )
    }
}

@Composable
fun TaskItem(
    title: String,
    isDone: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            ) 
        },
        leadingContent = {
            IconButton(onClick = { onCheckedChange(!isDone) }) {
                Icon(
                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isDone) "Erledigt" else "Offen",
                    tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
