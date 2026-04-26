package com.terminplaner.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import com.terminplaner.util.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppointmentsListUiState(
    val appointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val userName: String? = null,
    val isProUser: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class AppointmentsListViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentsListUiState())
    val uiState: StateFlow<AppointmentsListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appointmentRepository.getAllAppointments(),
                categoryRepository.getAllCategories(),
                themePreferences.userName,
                themePreferences.isProUser
            ) { appointments, categories, userName, isPro ->
                AppointmentsListUiState(
                    appointments = appointments,
                    categories = categories,
                    userName = userName,
                    isProUser = isPro,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
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
}
