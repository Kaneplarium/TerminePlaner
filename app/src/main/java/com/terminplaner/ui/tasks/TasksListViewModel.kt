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
    val isProUser: Boolean = false,
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

    init {
        viewModelScope.launch {
            combine(
                taskRepository.getAllTasks(),
                appointmentRepository.getAllAppointments(),
                themePreferences.userName,
                themePreferences.isProUser
            ) { tasks, appointments, userName, isPro ->
                TasksListUiState(
                    tasks = tasks,
                    appointments = appointments,
                    userName = userName,
                    isProUser = isPro
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
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
