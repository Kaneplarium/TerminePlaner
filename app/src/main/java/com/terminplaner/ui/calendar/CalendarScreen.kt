package com.terminplaner.ui.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.ui.components.AppTopBar
import com.terminplaner.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isBusiness = uiState.userStatus == ThemePreferences.STATUS_BUSINESS
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    rememberCoroutineScope()
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val animatedBackgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 300),
        label = "bg_color"
    )

    var showViewModesSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage) { 1000 }

    LaunchedEffect(pagerState.currentPage) {
        val diff = pagerState.currentPage - initialPage
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, diff)
        viewModel.selectDate(calendar.timeInMillis)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Terminplaner",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 28.dp))
                
                Text(
                    "Kalender",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                
                uiState.categories.forEach { category ->
                    var showColorPicker by remember { mutableStateOf(false) }
                    
                    NavigationDrawerItem(
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(category.name, modifier = Modifier.weight(1f))
                                IconButton(onClick = { showColorPicker = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Palette, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(if (uiState.selectedCategoryId == category.id) null else category.id) },
                        icon = { 
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(category.color)))
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    if (showColorPicker) {
                        ModalBottomSheet(onDismissRequest = { showColorPicker = false }) {
                            com.terminplaner.ui.components.ColorPicker(
                                selectedColor = category.color,
                                onColorSelected = { 
                                    viewModel.updateCategoryColor(category.id, it)
                                    showColorPicker = false 
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = animatedBackgroundColor,
            topBar = {
                AppTopBar(
                    areaName = SimpleDateFormat("EEE, d. MMM yyyy", Locale.getDefault()).format(Date(uiState.selectedDate)),
                    userStatus = uiState.userStatus,
                    navController = navController,
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        // Hamburger menu removed per user request
                    },
                    actions = {
                        // All actions removed per user request
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showViewModesSheet = true },
                    icon = { 
                        Icon(
                            imageVector = when(uiState.viewMode) {
                                CalendarViewMode.AGENDA -> Icons.Rounded.ViewAgenda
                                CalendarViewMode.DAY -> Icons.Rounded.ViewDay
                                CalendarViewMode.THREE_DAYS -> Icons.Rounded.ViewWeek
                                CalendarViewMode.WEEK -> Icons.Rounded.CalendarViewWeek
                                CalendarViewMode.MONTH -> Icons.Rounded.CalendarViewMonth
                            }, 
                            contentDescription = null
                        ) 
                    },
                    text = { 
                        Text(
                            text = when(uiState.viewMode) {
                                CalendarViewMode.AGENDA -> "Tagesübersicht"
                                CalendarViewMode.DAY -> "Tag"
                                CalendarViewMode.THREE_DAYS -> "3 Tage"
                                CalendarViewMode.WEEK -> "Woche"
                                CalendarViewMode.MONTH -> "Monat"
                            }
                        ) 
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { 
                                searchQuery = it
                                viewModel.setSearchQuery(it)
                            },
                            onSearch = { isSearchActive = false },
                            expanded = isSearchActive,
                            onExpandedChange = { isSearchActive = it },
                            placeholder = { Text("Suche") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = ""; viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            }
                        )
                    },
                    expanded = isSearchActive,
                    onExpandedChange = { isSearchActive = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) { }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = uiState.viewMode != CalendarViewMode.AGENDA && uiState.viewMode != CalendarViewMode.MONTH
                    ) {
                        when (uiState.viewMode) {
                            CalendarViewMode.AGENDA -> AgendaView(
                                appointments = uiState.allAppointments, 
                                tasks = uiState.tasks, 
                                onDeleteAppointment = { viewModel.deleteAppointment(it) },
                                onDeleteTask = { viewModel.deleteTask(it) }
                            )
                            CalendarViewMode.MONTH -> MonthView(uiState.selectedDate)
                            CalendarViewMode.DAY -> TimelineView(
                                appointments = uiState.appointments,
                                tasks = uiState.tasks,
                                isBusiness = isBusiness,
                                onTimeSlotClick = { hour, min ->
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = uiState.selectedDate
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, min)
                                    }
                                    navController.navigate(Screen.AppointmentEdit.createRoute(selectedDate = cal.timeInMillis))
                                },
                                onDeleteAppointment = { viewModel.deleteAppointment(it) }
                            )
                            CalendarViewMode.THREE_DAYS -> MultiDayView(uiState.allAppointments, uiState.tasks, 3, uiState.selectedDate, onDeleteAppointment = { viewModel.deleteAppointment(it) })
                            CalendarViewMode.WEEK -> MultiDayView(uiState.allAppointments, uiState.tasks, 7, uiState.selectedDate, onDeleteAppointment = { viewModel.deleteAppointment(it) })
                        }
                    }
                }
            }
        }
    }

    if (showViewModesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showViewModesSheet = false }
        ) {
            ViewModeSelectionContent(
                currentMode = uiState.viewMode,
                onModeSelected = { 
                    viewModel.setViewMode(it)
                    showViewModesSheet = false
                }
            )
        }
    }
}

@Composable
fun ViewModeSelectionContent(
    currentMode: CalendarViewMode,
    onModeSelected: (CalendarViewMode) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = "Ansicht wählen",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        ListItem(
            headlineContent = { Text("Tagesübersicht") },
            leadingContent = { Icon(Icons.Rounded.ViewAgenda, null) },
            trailingContent = { if(currentMode == CalendarViewMode.AGENDA) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onModeSelected(CalendarViewMode.AGENDA) }
        )
        ListItem(
            headlineContent = { Text("Tag") },
            leadingContent = { Icon(Icons.Rounded.ViewDay, null) },
            trailingContent = { if(currentMode == CalendarViewMode.DAY) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onModeSelected(CalendarViewMode.DAY) }
        )
        ListItem(
            headlineContent = { Text("3 Tage") },
            leadingContent = { Icon(Icons.Rounded.ViewWeek, null) },
            trailingContent = { if(currentMode == CalendarViewMode.THREE_DAYS) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onModeSelected(CalendarViewMode.THREE_DAYS) }
        )
        ListItem(
            headlineContent = { Text("Woche") },
            leadingContent = { Icon(Icons.Rounded.CalendarViewWeek, null) },
            trailingContent = { if(currentMode == CalendarViewMode.WEEK) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onModeSelected(CalendarViewMode.WEEK) }
        )
        ListItem(
            headlineContent = { Text("Monat") },
            leadingContent = { Icon(Icons.Rounded.CalendarViewMonth, null) },
            trailingContent = { if(currentMode == CalendarViewMode.MONTH) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.clickable { onModeSelected(CalendarViewMode.MONTH) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaView(
    appointments: List<Appointment>,
    tasks: List<com.terminplaner.domain.model.Task>,
    onDeleteAppointment: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    val groupedItems = (appointments.map { it to it.dateTime } + tasks.map { it to (it.reminderTime ?: 0L) })
        .filter { it.second > 0 }
        .sortedBy { it.second }
        .groupBy { 
            SimpleDateFormat("EEEE, d. MMMM", Locale.getDefault()).format(Date(it.second))
        }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedItems.forEach { (date, items) ->
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = date,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(items) { item ->
                val content = item.first
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.second))
                
                ListItem(
                    headlineContent = { 
                        Text(
                            text = if (content is Appointment) content.title else (content as com.terminplaner.domain.model.Task).title,
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    supportingContent = { 
                        if (content is Appointment && !content.location.isNullOrBlank()) {
                            Text(content.location)
                        }
                    },
                    leadingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(45.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (content is Appointment) {
                                            content.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary
                                        }
                                    )
                            )
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { 
                            if (content is Appointment) {
                                onDeleteAppointment(content.id)
                            } else if (content is com.terminplaner.domain.model.Task) {
                                onDeleteTask(content.id)
                            }
                        }) {
                            Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(modifier = Modifier.padding(start = 70.dp), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun MonthView(selectedDate: Long) {
    val calendar = Calendar.getInstance().apply { 
        timeInMillis = selectedDate
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val days = (0 until firstDayOfWeek).map { "" } + (1..daysInMonth).map { it.toString() }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize()
        ) {
            items(days) { day ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.isNotEmpty()) {
                        val isToday = day == SimpleDateFormat("d", Locale.getDefault()).format(Date()) && 
                                      SimpleDateFormat("MM yyyy", Locale.getDefault()).format(Date(selectedDate)) == 
                                      SimpleDateFormat("MM yyyy", Locale.getDefault()).format(Date())
                        
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultiDayView(
    allAppointments: List<Appointment>,
    tasks: List<com.terminplaner.domain.model.Task>,
    daysCount: Int,
    selectedDate: Long,
    onDeleteAppointment: (Long) -> Unit
) {
    val scrollState = rememberScrollState()
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    
    val days = (0 until daysCount).map {
        val d = calendar.clone() as Calendar
        d.add(Calendar.DAY_OF_YEAR, it)
        d
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 60.dp)) {
            days.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = SimpleDateFormat("E", Locale.getDefault()).format(day.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = day.get(Calendar.DAY_OF_MONTH).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))

        (8..20).forEach { hour ->
            Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = String.format(Locale.getDefault(), "%02d:00", hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                Row(modifier = Modifier.fillMaxSize().padding(start = 60.dp)) {
                    days.forEach { day ->
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            // Tasks on the line
                            tasks.filter { 
                                val taskCal = Calendar.getInstance().apply { it.reminderTime?.let { t -> timeInMillis = t } }
                                taskCal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR) &&
                                taskCal.get(Calendar.HOUR_OF_DAY) == hour
                            }.forEach { task ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                                    Spacer(Modifier.width(2.dp))
                                    Text(task.title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            allAppointments.filter { 
                                val apptCal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
                                apptCal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR) &&
                                apptCal.get(Calendar.HOUR_OF_DAY) == hour
                            }.forEach { appt ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp)
                                        .background(
                                            appt.color?.let { Color(it) }?.copy(alpha = 0.3f) ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(appt.title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { onDeleteAppointment(appt.id) }, modifier = Modifier.size(16.dp)) {
                                            Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineView(
    appointments: List<Appointment>,
    tasks: List<com.terminplaner.domain.model.Task>,
    isBusiness: Boolean,
    onTimeSlotClick: (Int, Int) -> Unit,
    onDeleteAppointment: (Long) -> Unit
) {
    val scrollState = rememberScrollState()
    val interval = if (isBusiness) 15 else 60
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        for (hour in 8..20) {
            for (min in 0 until 60 step interval) {
                val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", hour, min)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isBusiness) 40.dp else 80.dp)
                        .clickable { onTimeSlotClick(hour, min) }
                ) {
                    if (min == 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 60.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 0.dp)
                        )
                    } else if (isBusiness) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 60.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    appointments.filter { 
                        val cal = Calendar.getInstance().apply { timeInMillis = it.dateTime }
                        cal.get(Calendar.HOUR_OF_DAY) == hour && cal.get(Calendar.MINUTE) == min
                    }.forEach { appt ->
                        TimelineAppointmentBlock(appt, isBusiness, onDelete = { onDeleteAppointment(appt.id) })
                    }
                    
                    tasks.filter { 
                        val cal = Calendar.getInstance().apply { it.reminderTime?.let { t -> timeInMillis = t } }
                        cal.get(Calendar.HOUR_OF_DAY) == hour && cal.get(Calendar.MINUTE) == min
                    }.forEach { task ->
                        TimelineTaskItem(task)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineAppointmentBlock(appt: Appointment, isBusinessMode: Boolean, onDelete: () -> Unit) {
    val isApptBusiness = appt.isBusiness
    
    Card(
        modifier = Modifier
            .padding(start = 65.dp, end = 16.dp, top = 4.dp)
            .fillMaxWidth()
            .height(if (isBusinessMode) 32.dp else 70.dp),
        shape = if (isBusinessMode) RoundedCornerShape(4.dp) else RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isApptBusiness) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isApptBusiness) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            if (!isBusinessMode) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                Text(
                    text = appt.title,
                    style = if (isBusinessMode) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun TimelineTaskItem(task: com.terminplaner.domain.model.Task) {
    Row(
        modifier = Modifier
            .padding(start = 65.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { },
            modifier = Modifier.scale(0.7f)
        )
        Text(
            text = task.title,
            style = MaterialTheme.typography.labelMedium,
            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            maxLines = 1
        )
    }
}
