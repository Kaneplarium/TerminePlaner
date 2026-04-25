package com.terminplaner.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Task

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun setAlarm(time: Long, pendingIntent: PendingIntent) {
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }

    fun schedule(appointment: Appointment) {
        if (appointment.isCompleted || appointment.isDeleted) {
            cancel(appointment.id)
            cancelFocusMode(appointment.id)
            return
        }
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_APPOINTMENT_REMINDER"
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

        if (appointment.dateTime > System.currentTimeMillis()) {
            setAlarm(appointment.dateTime, pendingIntent)
        }

        // Schedule Focus Mode (DND)
        if (appointment.isFocusMode) {
            scheduleFocusMode(appointment)
        } else {
            cancelFocusMode(appointment.id)
        }
    }

    private fun scheduleFocusMode(appointment: Appointment) {
        // Start DND
        val startIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_START_FOCUS_MODE"
            putExtra("appointmentId", appointment.id)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            (appointment.id + 2000000).toInt(),
            startIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (appointment.dateTime > System.currentTimeMillis()) {
            setAlarm(appointment.dateTime, startPendingIntent)
        }

        // End DND
        val endIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_END_FOCUS_MODE"
            putExtra("appointmentId", appointment.id)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            (appointment.id + 3000000).toInt(),
            endIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (appointment.endDateTime > System.currentTimeMillis()) {
            setAlarm(appointment.endDateTime, endPendingIntent)
        }
    }

    private fun cancelFocusMode(appointmentId: Long) {
        val startIntent = Intent(context, NotificationReceiver::class.java).apply { action = "ACTION_START_FOCUS_MODE" }
        val startPendingIntent = PendingIntent.getBroadcast(context, (appointmentId + 2000000).toInt(), startIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
        if (startPendingIntent != null) alarmManager.cancel(startPendingIntent)

        val endIntent = Intent(context, NotificationReceiver::class.java).apply { action = "ACTION_END_FOCUS_MODE" }
        val endPendingIntent = PendingIntent.getBroadcast(context, (appointmentId + 3000000).toInt(), endIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
        if (endPendingIntent != null) alarmManager.cancel(endPendingIntent)
    }

    fun scheduleTaskReminder(task: Task) {
        if (task.isCompleted) {
            cancelTaskReminder(task.id)
            return
        }
        val time = task.reminderTime ?: return
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_TASK_REMINDER"
            putExtra("title", "Aufgabe: ${task.title}")
            putExtra("message", task.description ?: "Erinnerung an deine Aufgabe.")
            putExtra("taskId", task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 1000000).toInt(), // Offset task IDs to avoid collision with appointments
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (time > System.currentTimeMillis()) {
            setAlarm(time, pendingIntent)
        }
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 1000000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
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
