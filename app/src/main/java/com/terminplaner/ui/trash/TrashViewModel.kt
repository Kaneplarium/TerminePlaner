package com.terminplaner.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.util.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val deletedAppointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val themePreferences: ThemePreferences,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    val trashAutoDeleteDays = themePreferences.trashAutoDeleteDays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 30
    )

    val deleteLinkedTasks = themePreferences.deleteLinkedTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val userName = themePreferences.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val isProUser = themePreferences.isProUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        viewModelScope.launch {
            appointmentRepository.getDeletedAppointments().collect { appointments ->
                _uiState.update { it.copy(deletedAppointments = appointments, isLoading = false) }
            }
        }
    }

    fun setTrashAutoDeleteDays(days: Int) {
        viewModelScope.launch {
            themePreferences.setTrashAutoDeleteDays(days)
        }
    }

    fun setDeleteLinkedTasks(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDeleteLinkedTasks(enabled)
        }
    }

    fun restoreAppointment(id: Long) {
        viewModelScope.launch {
            appointmentRepository.restoreAppointment(id)
            dataExportManager.autoExport()
        }
    }

    fun permanentlyDelete(id: Long) {
        viewModelScope.launch {
            val deleteTasks = themePreferences.deleteLinkedTasks.first()
            appointmentRepository.permanentlyDeleteAppointment(id, deleteTasks)
            dataExportManager.autoExport()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            appointmentRepository.emptyTrash()
            dataExportManager.autoExport()
        }
    }
}