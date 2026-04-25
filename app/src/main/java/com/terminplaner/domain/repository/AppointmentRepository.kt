package com.terminplaner.domain.repository

import com.terminplaner.domain.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAllAppointments(): Flow<List<Appointment>>
    fun getAppointmentsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Appointment>>
    fun getDeletedAppointments(): Flow<List<Appointment>>
    suspend fun getAppointmentById(id: Long): Appointment?
    suspend fun insertAppointment(appointment: Appointment): Long
    suspend fun updateAppointment(appointment: Appointment)
    suspend fun softDeleteAppointment(id: Long, deleteTasks: Boolean = false)
    suspend fun restoreAppointment(id: Long)
    suspend fun permanentlyDeleteAppointment(id: Long, deleteTasks: Boolean = false)
    suspend fun emptyTrash()
    suspend fun deleteOldTrash(threshold: Long)
    suspend fun getOverlappingAppointments(start: Long, end: Long, excludeId: Long): List<Appointment>
    suspend fun getAllAppointmentsForExport(): List<Appointment>
    suspend fun importAppointments(appointments: List<Appointment>)
}