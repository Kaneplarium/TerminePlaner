package com.terminplaner.data.local.dao

import androidx.room.*
import com.terminplaner.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE isDeleted = 0 ORDER BY dateTime ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE isDeleted = 0 AND dateTime >= :startOfDay AND dateTime < :endOfDay ORDER BY dateTime ASC")
    fun getAppointmentsByDate(startOfDay: Long, endOfDay: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteAppointment(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE appointments SET isDeleted = 0, deletedAt = null WHERE id = :id")
    suspend fun restoreAppointment(id: Long)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun permanentlyDeleteAppointment(id: Long)

    @Query("DELETE FROM appointments WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM appointments WHERE isDeleted = 1 AND deletedAt < :threshold")
    suspend fun deleteOldTrash(threshold: Long)

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointmentsForExport(): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE isDeleted = 0 AND id != :excludeId AND ((dateTime < :end AND endDateTime > :start))")
    suspend fun getOverlappingAppointments(start: Long, end: Long, excludeId: Long): List<AppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)
}