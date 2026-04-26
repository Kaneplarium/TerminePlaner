package com.terminplaner.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Task
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.TaskRepository
import com.terminplaner.util.AlarmScheduler
import com.terminplaner.util.DataExportManager
import com.terminplaner.util.MLManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskEditUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    val isBusiness: Boolean = false,
    val appointmentId: Long? = null,
    val reminderTime: Long? = null,
    val appointments: List<Appointment> = emptyList(),
    val userName: String? = null,
    val userStatus: Int = ThemePreferences.STATUS_NONE,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: Boolean = false,
    val suggestedReminderTime: Long? = null
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val appointmentRepository: AppointmentRepository,
    private val themePreferences: ThemePreferences,
    private val alarmScheduler: AlarmScheduler,
    private val dataExportManager: DataExportManager,
    private val mlManager: MLManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0
    private val initialAppointmentId: Long = savedStateHandle.get<Long>("appointmentId") ?: 0

    private val _uiState = MutableStateFlow(TaskEditUiState(
        isEditMode = taskId > 0,
        appointmentId = if (initialAppointmentId > 0) initialAppointmentId else null
    ))
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appointmentRepository.getAllAppointments().collect { appointments ->
                _uiState.update { it.copy(appointments = appointments) }
            }
        }

        viewModelScope.launch {
            themePreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            themePreferences.userStatus.collect { status ->
                _uiState.update { it.copy(userStatus = status) }
            }
        }

        if (taskId > 0) {
            loadTask(taskId)
        }
    }

    fun loadTask(id: Long) {
        viewModelScope.launch {
            taskRepository.getTaskById(id)?.let { task ->
                _uiState.update {
                    it.copy(
                        id = task.id,
                        title = task.title,
                        description = task.description ?: "",
                        isCompleted = task.isCompleted,
                        isImportant = task.isImportant,
                        isBusiness = task.isBusiness,
                        appointmentId = task.appointmentId,
                        reminderTime = task.reminderTime,
                        isEditMode = true
                    )
                }
            }
        }
    }

    fun updateReminderTime(time: Long?) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = false) }
        checkForSuggestions(title)
    }

    private fun checkForSuggestions(text: String) {
        viewModelScope.launch {
            val dateTime = mlManager.extractDateTime(text)
            _uiState.update { it.copy(suggestedReminderTime = dateTime) }
        }
    }

    fun applySuggestion() {
        val suggestion = _uiState.value.suggestedReminderTime ?: return
        updateReminderTime(suggestion)
        _uiState.update { it.copy(suggestedReminderTime = null) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateAppointment(appointmentId: Long?) {
        _uiState.update { it.copy(appointmentId = appointmentId) }
    }

    fun updateBusiness(isBusiness: Boolean) {
        _uiState.update { it.copy(isBusiness = isBusiness) }
    }

    fun updateImportant(isImportant: Boolean) {
        _uiState.update { it.copy(isImportant = isImportant) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            val task = Task(
                id = state.id,
                title = state.title,
                description = state.description.ifBlank { null },
                isCompleted = state.isCompleted,
                isImportant = state.isImportant,
                isBusiness = state.isBusiness,
                appointmentId = state.appointmentId,
                reminderTime = state.reminderTime
            )

            val taskId = if (state.isEditMode) {
                taskRepository.updateTask(task)
                state.id
            } else {
                taskRepository.insertTask(task)
            }
            
            val finalTask = task.copy(id = taskId)
            if (finalTask.reminderTime != null) {
                alarmScheduler.scheduleTaskReminder(finalTask)
            } else {
                alarmScheduler.cancelTaskReminder(finalTask.id)
            }

            dataExportManager.autoExport()
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
