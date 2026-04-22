package com.terminplaner.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
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

data class ExportData(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val appointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList()
)

data class SettingsUiState(
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null,
    val selectedThemeColor: Long = 0xFF2196F3
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
        initialValue = 0xFF2196F3
    )

    fun setThemeColor(color: Long) {
        viewModelScope.launch {
            themePreferences.setThemeColor(color)
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