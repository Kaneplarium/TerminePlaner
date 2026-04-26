package com.terminplaner.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Task
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.TaskRepository
import com.terminplaner.util.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksListUiState(
    val tasks: List<Task> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val userName: String? = null,
    val userStatus: Int = ThemePreferences.STATUS_NONE,
    val focusFilter: Int = ThemePreferences.FILTER_ALL,
    val isLoading: Boolean = false
)

@HiltViewModel
class TasksListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val appointmentRepository: AppointmentRepository,
    private val themePreferences: ThemePreferences,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksListUiState())
    val uiState: StateFlow<TasksListUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                listOf(
                    taskRepository.getAllTasks(),
                    appointmentRepository.getAllAppointments(),
                    _searchQuery,
                    themePreferences.userName,
                    themePreferences.userStatus,
                    themePreferences.focusFilter
                )
            ) { array ->
                val tasks = array[0] as List<Task>
                val appointments = array[1] as List<Appointment>
                val query = array[2] as String
                val userName = array[3] as String?
                val status = array[4] as Int
                val filter = array[5] as Int

                val filteredTasks = tasks.filter { task ->
                    val matchesStatus = if (status == ThemePreferences.STATUS_BUSINESS) task.isBusiness else !task.isBusiness
                    val matchesSearch = query.isBlank() || task.title.contains(query, ignoreCase = true)
                    matchesStatus && matchesSearch
                }
                
                TasksListUiState(
                    tasks = filteredTasks,
                    appointments = appointments,
                    userName = userName,
                    userStatus = status,
                    focusFilter = filter
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setFocusFilter(filter: Int) {
        viewModelScope.launch {
            themePreferences.setFocusFilter(filter)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task.id, !task.isCompleted)
            dataExportManager.autoExport()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            dataExportManager.autoExport()
        }
    }
}
