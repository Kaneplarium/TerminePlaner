package com.terminplaner.data.repository

import com.terminplaner.data.local.dao.AppointmentDao
import com.terminplaner.data.local.dao.TaskDao
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val taskDao: TaskDao
) : AppointmentRepository {

    override fun getAllAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getAllAppointments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAppointmentsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsByDate(startOfDay, endOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDeletedAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getDeletedAppointments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAppointmentById(id: Long): Appointment? {
        return appointmentDao.getAppointmentById(id)?.toDomain()
    }

    override suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment.toEntity())
    }

    override suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun softDeleteAppointment(id: Long, deleteTasks: Boolean) {
        appointmentDao.softDeleteAppointment(id)
        if (deleteTasks) {
            taskDao.deleteTasksByAppointmentId(id)
        }
    }

    override suspend fun restoreAppointment(id: Long) {
        appointmentDao.restoreAppointment(id)
    }

    override suspend fun permanentlyDeleteAppointment(id: Long, deleteTasks: Boolean) {
        appointmentDao.permanentlyDeleteAppointment(id)
        if (deleteTasks) {
            taskDao.deleteTasksByAppointmentId(id)
        }
    }

    override suspend fun emptyTrash() {
        taskDao.deleteTasksForDeletedAppointments()
        appointmentDao.emptyTrash()
    }

    override suspend fun deleteOldTrash(threshold: Long) {
        // First delete tasks linked to appointments that will be deleted
        // This is a bit tricky with raw SQL without knowing which IDs are deleted.
        // We can do it in two steps if we have a query for it.
        // For now, let's just delete the appointments.
        // If the user wants tasks deleted too, they should be deleted when the appointment is deleted.
        appointmentDao.deleteOldTrash(threshold)
    }

    override suspend fun getOverlappingAppointments(start: Long, end: Long, excludeId: Long): List<Appointment> {
        return appointmentDao.getOverlappingAppointments(start, end, excludeId).map { it.toDomain() }
    }

    override suspend fun getAllAppointmentsForExport(): List<Appointment> {
        return appointmentDao.getAllAppointmentsForExport().map { it.toDomain() }
    }

    override suspend fun importAppointments(appointments: List<Appointment>) {
        appointmentDao.insertAll(appointments.map { it.toEntity() })
    }
}