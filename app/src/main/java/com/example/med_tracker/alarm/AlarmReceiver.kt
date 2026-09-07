package com.example.med_tracker.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(NotificationActionReceiver.EXTRA_LOG_ID, -1L)
        val medicationId = intent.getLongExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME) ?: "Лекарство"
        val dosage = intent.getStringExtra(NotificationActionReceiver.EXTRA_DOSAGE) ?: ""
        val snoozeMinutes = intent.getIntExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, 10)

        if (logId != -1L) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.showIntakeNotification(
                context = context,
                logId = logId,
                medicationId = medicationId,
                medicationName = medicationName,
                dosage = dosage,
                snoozeMinutes = snoozeMinutes
            )
        }
    }
}