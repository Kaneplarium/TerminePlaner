package com.terminplaner.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import com.terminplaner.domain.repository.TaskRepository
import com.terminplaner.util.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class CalendarViewMode {
    AGENDA, DAY, THREE_DAYS, WEEK, MONTH
}

data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val appointments: List<Appointment> = emptyList(),
    val allAppointments: List<Appointment> = emptyList(),
    val tasks: List<com.terminplaner.domain.model.Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val userName: String? = null,
    val userStatus: Int = ThemePreferences.STATUS_NONE,
    val focusFilter: Int = ThemePreferences.FILTER_ALL,
    val viewMode: CalendarViewMode = CalendarViewMode.DAY,
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _viewMode = MutableStateFlow(CalendarViewMode.DAY)

    init {
        viewModelScope.launch {
            val days = themePreferences.trashAutoDeleteDays.first()
            val threshold = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000)
            appointmentRepository.deleteOldTrash(threshold)
        }

        viewModelScope.launch {
            combine(
                listOf(
                    _selectedDate,
                    categoryRepository.getAllCategories(),
                    appointmentRepository.getAllAppointments(),
                    taskRepository.getAllTasks(),
                    _selectedCategoryId,
                    _searchQuery,
                    themePreferences.userName,
                    themePreferences.userStatus,
                    themePreferences.focusFilter,
                    _viewMode
                )
            ) { array ->
                val date = array[0] as Long
                val categories = array[1] as List<Category>
                val allAppts = array[2] as List<Appointment>
                val allTasks = array[3] as List<com.terminplaner.domain.model.Task>
                val selectedCatId = array[4] as Long?
                val query = array[5] as String
                val userName = array[6] as String?
                val status = array[7] as Int
                val filter = array[8] as Int
                val mode = array[9] as CalendarViewMode

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.timeInMillis

                // Filter logic (Complete separation)
                val filteredAppts = allAppts.filter { appt ->
                    val matchesCategory = selectedCatId == null || appt.categoryId == selectedCatId
                    val isApptBusiness = appt.isBusiness
                    val matchesStatus = if (status == ThemePreferences.STATUS_BUSINESS) isApptBusiness else !isApptBusiness
                    
                    val matchesSearch = query.isBlank() || appt.title.contains(query, ignoreCase = true) || (appt.description?.contains(query, ignoreCase = true) == true)
                    
                    matchesCategory && matchesStatus && matchesSearch
                }

                val dayAppts = filteredAppts.filter { 
                    it.dateTime >= startOfDay && it.dateTime < endOfDay 
                }
                
                val dayTasks = allTasks.filter { task ->
                    val taskTime = task.reminderTime ?: 0L
                    val matchesStatus = if (status == ThemePreferences.STATUS_BUSINESS) task.isBusiness else !task.isBusiness
                    val matchesSearch = query.isBlank() || task.title.contains(query, ignoreCase = true)
                    
                    taskTime >= startOfDay && taskTime < endOfDay && matchesStatus && matchesSearch
                }

                CalendarUiState(
                    selectedDate = date,
                    appointments = dayAppts,
                    allAppointments = filteredAppts,
                    tasks = dayTasks,
                    categories = categories,
                    selectedCategoryId = selectedCatId,
                    userName = userName,
                    userStatus = status,
                    focusFilter = filter,
                    viewMode = mode
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectDate(date: Long) {
        _selectedDate.value = date
    }

    fun goToToday() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        _selectedDate.value = today
    }

    fun setFocusFilter(filter: Int) {
        viewModelScope.launch {
            themePreferences.setFocusFilter(filter)
        }
    }

    fun updateCategoryColor(categoryId: Long, color: Int) {
        viewModelScope.launch {
            categoryRepository.getCategoryById(categoryId)?.let {
                categoryRepository.updateCategory(it.copy(color = color))
            }
        }
    }

    fun deleteAppointment(id: Long) {
        viewModelScope.launch {
            val deleteTasks = themePreferences.deleteLinkedTasks.first()
            appointmentRepository.softDeleteAppointment(id, deleteTasks)
            dataExportManager.autoExport()
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
            dataExportManager.autoExport()
        }
    }
}
