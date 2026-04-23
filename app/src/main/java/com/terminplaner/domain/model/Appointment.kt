package com.terminplaner.domain.model

data class Appointment(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dateTime: Long,
    val endDateTime: Long,
    val categoryId: Long? = null,
    val color: Int? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)