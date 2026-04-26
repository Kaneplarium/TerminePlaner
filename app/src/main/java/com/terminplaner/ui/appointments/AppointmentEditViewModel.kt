package com.terminplaner.ui.appointments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import com.terminplaner.util.AlarmScheduler
import com.terminplaner.util.DataExportManager
import com.terminplaner.util.MLManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AppointmentEditUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val persons: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val isConfirmed: Boolean = false,
    val dateTime: Long = System.currentTimeMillis(),
    val endDateTime: Long = System.currentTimeMillis() + 3600000, // + 1 hour
    val categoryId: Long? = null,
    val color: Int? = null,
    val isFocusMode: Boolean = false,
    val isBusiness: Boolean = false,
    val categories: List<Category> = emptyList(),
    val userName: String? = null,
    val userStatus: Int = ThemePreferences.STATUS_NONE,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: Boolean = false,
    val hasOverlap: Boolean = false,
    val showOverlapDialog: Boolean = false,
    val isPastDateError: Boolean = false,
    val suggestedDateTime: Long? = null
)

@HiltViewModel
class AppointmentEditViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences,
    private val alarmScheduler: AlarmScheduler,
    private val dataExportManager: DataExportManager,
    private val mlManager: MLManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appointmentId: Long = savedStateHandle.get<Long>("appointmentId") ?: 0
    private val selectedDateArg: Long = savedStateHandle.get<Long>("selectedDate") ?: 0
    private val selectedDate: Long = if (selectedDateArg > 0) selectedDateArg else System.currentTimeMillis()

    private val _uiState = MutableStateFlow(AppointmentEditUiState(isEditMode = appointmentId > 0))
    val uiState: StateFlow<AppointmentEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }

        viewModelScope.launch {
            themePreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            themePreferences.userStatus.collect { status ->
                val focus = if (status == ThemePreferences.STATUS_BUSINESS) true else _uiState.value.isFocusMode
                val isBiz = status == ThemePreferences.STATUS_BUSINESS
                _uiState.update { it.copy(userStatus = status, isFocusMode = focus, isBusiness = isBiz) }
            }
        }

        viewModelScope.launch {
            if (appointmentId > 0) {
                appointmentRepository.getAppointmentById(appointmentId)?.let { appointment ->
                    _uiState.update {
                        it.copy(
                            id = appointment.id,
                            title = appointment.title,
                            description = appointment.description ?: "",
                            location = appointment.location ?: "",
                            persons = appointment.persons ?: "",
                            customerName = appointment.customerName ?: "",
                            customerEmail = appointment.customerEmail ?: "",
                            isConfirmed = appointment.isConfirmed,
                            dateTime = appointment.dateTime,
                            endDateTime = appointment.endDateTime,
                            categoryId = appointment.categoryId,
                            color = appointment.color,
                            isFocusMode = appointment.isFocusMode
                        )
                    }
                    checkForOverlap()
                }
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                val endCalendar = Calendar.getInstance().apply {
                    timeInMillis = calendar.timeInMillis
                    add(Calendar.HOUR_OF_DAY, 1)
                }
                _uiState.update { 
                    it.copy(
                        dateTime = calendar.timeInMillis,
                        endDateTime = endCalendar.timeInMillis
                    ) 
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = false) }
        checkForSuggestions(title)
        autoSave()
    }

    private fun checkForSuggestions(text: String) {
        viewModelScope.launch {
            val dateTime = mlManager.extractDateTime(text)
            _uiState.update { it.copy(suggestedDateTime = dateTime) }
        }
    }

    fun applySuggestion() {
        val suggestion = _uiState.value.suggestedDateTime ?: return
        updateDateTime(suggestion)
        _uiState.update { it.copy(suggestedDateTime = null) }
    }

    fun processFlyer(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            val text = mlManager.recognizeText(bitmap) ?: return@launch
            _uiState.update { it.copy(description = text) }
            val dateTime = mlManager.extractDateTime(text)
            if (dateTime != null) {
                _uiState.update { it.copy(suggestedDateTime = dateTime) }
            }
        }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
        autoSave()
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(location = location) }
        autoSave()
    }

    fun updatePersons(persons: String) {
        _uiState.update { it.copy(persons = persons) }
        autoSave()
    }

    fun updateCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
        autoSave()
    }

    fun updateCustomerEmail(email: String) {
        _uiState.update { it.copy(customerEmail = email) }
        autoSave()
    }

    fun updateConfirmation(confirmed: Boolean) {
        _uiState.update { it.copy(isConfirmed = confirmed) }
        autoSave()
    }

    fun updateDateTime(dateTime: Long) {
        val duration = _uiState.value.endDateTime - _uiState.value.dateTime
        _uiState.update { 
            it.copy(
                dateTime = dateTime, 
                endDateTime = dateTime + duration,
                isPastDateError = isPast(dateTime)
            ) 
        }
        checkForOverlap()
        autoSave()
    }

    private fun isPast(millis: Long): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return millis < today
    }

    fun updateEndDateTime(endDateTime: Long) {
        _uiState.update { it.copy(endDateTime = endDateTime) }
        checkForOverlap()
        autoSave()
    }

    private fun checkForOverlap() {
        val state = _uiState.value
        viewModelScope.launch {
            val overlapping = appointmentRepository.getOverlappingAppointments(
                state.dateTime,
                state.endDateTime,
                state.id
            )
            _uiState.update { it.copy(hasOverlap = overlapping.isNotEmpty()) }
        }
    }

    fun updateCategory(categoryId: Long?) {
        _uiState.update { it.copy(categoryId = categoryId) }
        autoSave()
    }

    fun updateColor(color: Int?) {
        _uiState.update { it.copy(color = color) }
        autoSave()
    }

    fun updateFocusMode(isFocus: Boolean) {
        if (_uiState.value.userStatus == ThemePreferences.STATUS_BUSINESS) return
        _uiState.update { it.copy(isFocusMode = isFocus) }
        autoSave()
    }

    private var saveJob: kotlinx.coroutines.Job? = null

    private fun autoSave() {
        val state = _uiState.value
        if (state.title.isBlank() || state.isPastDateError) return

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce save
            
            val appointment = Appointment(
                id = state.id,
                title = state.title,
                description = state.description.ifBlank { null },
                location = state.location.ifBlank { null },
                persons = state.persons.ifBlank { null },
                customerName = state.customerName.ifBlank { null },
                customerEmail = state.customerEmail.ifBlank { null },
                isConfirmed = state.isConfirmed,
                dateTime = state.dateTime,
                endDateTime = state.endDateTime,
                categoryId = state.categoryId,
                color = state.color,
                isFocusMode = state.isFocusMode,
                isBusiness = state.isBusiness
            )

            if (state.isEditMode) {
                appointmentRepository.updateAppointment(appointment)
            } else {
                val id = appointmentRepository.insertAppointment(appointment)
                _uiState.update { it.copy(id = id, isEditMode = true) }
            }
            
            val updatedState = _uiState.value
            alarmScheduler.schedule(appointment.copy(id = updatedState.id))
            dataExportManager.autoExport()
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        if (!state.isEditMode && isPast(state.dateTime)) {
            _uiState.update { it.copy(isPastDateError = true) }
            return
        }

        if (state.hasOverlap) {
            _uiState.update { it.copy(showOverlapDialog = true) }
        } else {
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun confirmOverlapSave() {
        _uiState.update { it.copy(showOverlapDialog = false, isSaved = true) }
    }

    fun dismissOverlapDialog() {
        _uiState.update { it.copy(showOverlapDialog = false) }
    }

    // Deprecated, use onSaveClick or autoSave
    fun save() = onSaveClick()
}
