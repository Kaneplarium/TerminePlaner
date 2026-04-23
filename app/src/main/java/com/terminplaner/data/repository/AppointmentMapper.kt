package com.terminplaner.data.repository

import com.terminplaner.data.local.entity.AppointmentEntity
import com.terminplaner.domain.model.Appointment

fun AppointmentEntity.toDomain(): Appointment = Appointment(
    id = id,
    title = title,
    description = description,
    dateTime = dateTime,
    endDateTime = endDateTime,
    categoryId = categoryId,
    color = color,
    isDeleted = isDeleted,
    deletedAt = deletedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Appointment.toEntity(): AppointmentEntity = AppointmentEntity(
    id = id,
    title = title,
    description = description,
    dateTime = dateTime,
    endDateTime = endDateTime,
    categoryId = categoryId,
    color = color,
    isDeleted = isDeleted,
    deletedAt = deletedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)