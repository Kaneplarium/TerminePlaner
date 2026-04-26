package com.terminplaner.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.model.ExportData
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null,
    val selectedThemeColor: Long = 0xFF007AFF,
    val dynamicColor: Boolean = true,
    val userName: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val themeColor = themePreferences.themeColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0xFF007AFF
    )

    val darkThemeMode = themePreferences.darkThemeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreferences.MODE_SYSTEM
    )

    val dynamicColor = themePreferences.dynamicColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val userName = themePreferences.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val profilePictureUri = themePreferences.profilePictureUri.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val personalAddress = themePreferences.personalAddress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val personalBirthday = themePreferences.personalBirthday.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val employerName = themePreferences.employerName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val employerAddress = themePreferences.employerAddress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val employerPLZ = themePreferences.employerPLZ.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val employerCity = themePreferences.employerCity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    val isFirstRunCompleted = themePreferences.isFirstRunCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val userStatus = themePreferences.userStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreferences.STATUS_NONE
    )

    val focusFilter = themePreferences.focusFilter.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreferences.FILTER_ALL
    )

    val calendarVisibility = themePreferences.calendarVisibility.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreferences.VISIBILITY_PRIVATE
    )

    val smartwatchSync = themePreferences.smartwatchSync.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    // Personal Flows
    val showWeekend = themePreferences.showWeekend.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val showHolidays = themePreferences.showHolidays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val travelTime = themePreferences.travelTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val defaultReminder = themePreferences.defaultReminder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)
    val importBirthdays = themePreferences.importBirthdays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val allowInvitations = themePreferences.allowInvitations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Business Flows
    val bizRecurring = themePreferences.bizRecurring.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val bizConfirmations = themePreferences.bizConfirmations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setThemeColor(color: Long) = viewModelScope.launch { themePreferences.setThemeColor(color) }
    fun setDarkMode(mode: Int) = viewModelScope.launch { themePreferences.setDarkMode(mode) }
    fun setUserName(name: String?) = viewModelScope.launch { themePreferences.setUserName(name) }
    fun setProfilePictureUri(uri: String?) = viewModelScope.launch { themePreferences.setProfilePictureUri(uri) }
    
    fun setPersonalData(address: String?, birthday: Long?) = viewModelScope.launch {
        themePreferences.setPersonalData(address, birthday)
    }

    fun setEmployerData(name: String?, address: String?, plz: String?, city: String?) = viewModelScope.launch { 
        themePreferences.setEmployerData(name, address, plz, city) 
    }

    fun setTrashAutoDeleteDays(days: Int) = viewModelScope.launch { themePreferences.setTrashAutoDeleteDays(days) }
    fun setDeleteLinkedTasks(enabled: Boolean) = viewModelScope.launch { themePreferences.setDeleteLinkedTasks(enabled) }
    fun setUserStatus(status: Int) = viewModelScope.launch { themePreferences.setUserStatus(status) }
    fun setFocusFilter(filter: Int) = viewModelScope.launch { themePreferences.setFocusFilter(filter) }
    fun setCalendarVisibility(visibility: Int) = viewModelScope.launch { themePreferences.setCalendarVisibility(visibility) }
    fun setSmartwatchSync(enabled: Boolean) = viewModelScope.launch { themePreferences.setSmartwatchSync(enabled) }
    
    // Personal Setters
    fun setShowWeekend(enabled: Boolean) = viewModelScope.launch { themePreferences.setShowWeekend(enabled) }
    fun setShowHolidays(enabled: Boolean) = viewModelScope.launch { themePreferences.setShowHolidays(enabled) }
    fun setTravelTime(enabled: Boolean) = viewModelScope.launch { themePreferences.setTravelTime(enabled) }
    fun setDefaultReminder(minutes: Int) = viewModelScope.launch { themePreferences.setDefaultReminder(minutes) }
    fun setImportBirthdays(enabled: Boolean) = viewModelScope.launch { themePreferences.setImportBirthdays(enabled) }
    fun setAllowInvitations(enabled: Boolean) = viewModelScope.launch { themePreferences.setAllowInvitations(enabled) }

    // Business Setters
    fun setBizRecurring(enabled: Boolean) = viewModelScope.launch { themePreferences.setBizRecurring(enabled) }
    fun setBizConfirmations(enabled: Boolean) = viewModelScope.launch { themePreferences.setBizConfirmations(enabled) }

    fun updateCategoryColor(categoryId: Long, color: Int) {
        viewModelScope.launch {
            categoryRepository.getCategoryById(categoryId)?.let {
                categoryRepository.updateCategory(it.copy(color = color))
            }
        }
    }

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val appointments = appointmentRepository.getAllAppointmentsForExport()
                val categories = categoryRepository.getAllCategoriesForExport()

                val exportData = ExportData(
                    appointments = appointments,
                    categories = categories
                )

                val json = gson.toJson(exportData)
                val fileName = "terminplaner_export_${System.currentTimeMillis()}.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(json)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Termine exportieren"))

                _uiState.update { it.copy(exportSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun importData(json: String) {
        viewModelScope.launch {
            try {
                val exportData = gson.fromJson(json, ExportData::class.java)

                if (exportData.categories.isNotEmpty()) {
                    categoryRepository.importCategories(exportData.categories)
                }
                if (exportData.appointments.isNotEmpty()) {
                    appointmentRepository.importAppointments(exportData.appointments)
                }

                _uiState.update { it.copy(importSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
