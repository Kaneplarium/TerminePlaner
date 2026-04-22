package com.terminplaner.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val currentMonth: Long = System.currentTimeMillis(),
    val appointments: List<Appointment> = emptyList(),
    val allAppointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository
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
    private val _currentMonth = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )

    init {
        viewModelScope.launch {
            combine(
                _selectedDate,
                _currentMonth,
                categoryRepository.getAllCategories(),
                appointmentRepository.getAllAppointments()
            ) { date, month, categories, allAppointments ->
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

                val dayAppointments = allAppointments.filter { 
                    it.dateTime >= startOfDay && it.dateTime < endOfDay 
                }

                CalendarUiState(
                    selectedDate = date,
                    currentMonth = month,
                    appointments = dayAppointments,
                    allAppointments = allAppointments,
                    categories = categories
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectDate(date: Long) {
        _selectedDate.value = date
    }

    fun setCurrentMonth(month: Long) {
        _currentMonth.value = month
    }

    fun nextMonth() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _currentMonth.value
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
        }
        _currentMonth.value = calendar.timeInMillis
    }

    fun previousMonth() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _currentMonth.value
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -1)
        }
        _currentMonth.value = calendar.timeInMillis
    }

    fun deleteAppointment(id: Long) {
        viewModelScope.launch {
            appointmentRepository.softDeleteAppointment(id)
        }
    }
}
