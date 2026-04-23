package com.terminplaner.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.terminplaner.domain.model.Appointment
import java.text.SimpleDateFormat
import java.util.*

object EmailHelper {

    fun sendAppointmentEmail(context: Context, appointment: Appointment) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateString = sdf.format(Date(appointment.dateTime))
        
        val subject = "Erinnerung: ${appointment.title}"
        val body = """
            Hallo,
            
            dies ist eine Erinnerung an deinen Termin:
            
            Titel: ${appointment.title}
            Datum/Uhrzeit: $dateString
            Beschreibung: ${appointment.description ?: "Keine Beschreibung vorhanden"}
            
            Gesendet von TerminePlaner.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback if no email app is installed
            val chooser = Intent.createChooser(intent, "Email senden...")
            context.startActivity(chooser)
        }
    }
}
