package com.terminplaner.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.ExportData
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun autoExport() = withContext(Dispatchers.IO) {
        try {
            val appointments = appointmentRepository.getAllAppointmentsForExport()
            val categories = categoryRepository.getAllCategoriesForExport()

            val exportData = ExportData(
                appointments = appointments,
                categories = categories
            )

            val json = gson.toJson(exportData)
            
            // Internal backup (as before)
            val internalFile = File(context.filesDir, "auto_export_terminplaner.json")
            internalFile.writeText(json)

            // External rotation backup (3 files)
            val storagePath = themePreferences.storagePath.first() ?: return@withContext
            val treeUri = Uri.parse(storagePath)
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext
            
            var terminplanerDir = rootDoc.findFile("Terminplaner")
            if (terminplanerDir == null || !terminplanerDir.isDirectory) {
                terminplanerDir = rootDoc.createDirectory("Terminplaner")
            }
            
            if (terminplanerDir != null) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "backup_$timestamp.json"
                
                // Create new backup
                val newFile = terminplanerDir.createFile("application/json", fileName)
                newFile?.uri?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                }

                // Rotation: Keep only 3 latest files
                val files = terminplanerDir.listFiles()
                    .filter { it.name?.startsWith("backup_") == true && it.name?.endsWith(".json") == true }
                    .sortedByDescending { it.name }
                
                if (files.size > 3) {
                    files.drop(3).forEach { it.delete() }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun exportData(context: Context, json: String) {
        // Manual export logic if needed
    }
}
