package com.terminplaner.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.terminplaner.domain.repository.AppointmentRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appointmentRepository: AppointmentRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarms(context)
        } else {
            val title = intent.getStringExtra("title") ?: "Termin"
            val message = intent.getStringExtra("message") ?: "Du hast einen anstehenden Termin."
            val appointmentId = intent.getLongExtra("appointmentId", -1L)

            if (appointmentId != -1L) {
                NotificationHelper(context).showNotification(title, message, appointmentId)
            }
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val scheduler = AlarmScheduler(context)
        CoroutineScope(Dispatchers.IO).launch {
            val appointments = appointmentRepository.getAllAppointmentsForExport() // Re-using this or similar
            appointments.filter { !it.isDeleted && it.dateTime > System.currentTimeMillis() }
                .forEach { scheduler.schedule(it) }
        }
    }
}
