package com.terminplaner.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.terminplaner.domain.model.Appointment

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(appointment: Appointment) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", appointment.title)
            putExtra("message", appointment.description ?: "Du hast einen Termin.")
            putExtra("appointmentId", appointment.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Schedule only if the appointment is in the future
        if (appointment.dateTime > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                appointment.dateTime,
                pendingIntent
            )
        }
    }

    fun cancel(appointmentId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
