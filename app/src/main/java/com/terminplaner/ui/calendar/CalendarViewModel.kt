package com.terminplaner.ui.calendar

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
import java.util.Calendar
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val currentMonth: Long = System.currentTimeMillis(),
    val appointments: List<Appointment> = emptyList(),
    val allAppointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val userName: String? = null,
    val userStatus: Int = ThemePreferences.STATUS_NONE,
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
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
    private val _currentMonth = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)

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
                    _currentMonth,
                    categoryRepository.getAllCategories(),
                    appointmentRepository.getAllAppointments(),
                    _selectedCategoryId,
                    themePreferences.userName,
                    themePreferences.userStatus
                )
            ) { array ->
                val date = array[0] as Long
                val month = array[1] as Long
                val categories = array[2] as List<Category>
                val allAppointments = array[3] as List<Appointment>
                val selectedCatId = array[4] as Long?
                val userName = array[5] as String?
                val status = array[6] as Int

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

                val filteredAll = if (selectedCatId != null) {
                    allAppointments.filter { it.categoryId == selectedCatId }
                } else {
                    allAppointments
                }

                val dayAppointments = filteredAll.filter { 
                    it.dateTime >= startOfDay && it.dateTime < endOfDay 
                }

                CalendarUiState(
                    selectedDate = date,
                    currentMonth = month,
                    appointments = dayAppointments,
                    allAppointments = filteredAll,
                    categories = categories,
                    selectedCategoryId = selectedCatId,
                    userName = userName,
                    userStatus = status
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
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

    fun nextWeek() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value
            add(Calendar.DAY_OF_YEAR, 7)
        }
        _selectedDate.value = calendar.timeInMillis
    }

    fun previousWeek() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value
            add(Calendar.DAY_OF_YEAR, -7)
        }
        _selectedDate.value = calendar.timeInMillis
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
            val deleteTasks = themePreferences.deleteLinkedTasks.first()
            appointmentRepository.softDeleteAppointment(id, deleteTasks)
            dataExportManager.autoExport()
        }
    }
}
