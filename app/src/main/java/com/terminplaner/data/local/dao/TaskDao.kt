package com.terminplaner.data.local.dao

import androidx.room.*
import com.terminplaner.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE appointmentId = :appointmentId ORDER BY createdAt DESC")
    fun getTasksByAppointmentId(appointmentId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM tasks WHERE appointmentId = :appointmentId")
    suspend fun deleteTasksByAppointmentId(appointmentId: Long)

    @Query("DELETE FROM tasks WHERE appointmentId IN (SELECT id FROM appointments WHERE isDeleted = 1)")
    suspend fun deleteTasksForDeletedAppointments()

    @Query("UPDATE tasks SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleTaskCompletion(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())
}
