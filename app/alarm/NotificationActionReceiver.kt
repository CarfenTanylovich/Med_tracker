package com.example.med_tracker.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.IntakeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKEN = "com.example.med_tracker.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.example.med_tracker.ACTION_SNOOZE"

        const val EXTRA_LOG_ID = "extra_log_id"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"

        private const val SNOOZE_DELAY_MILLIS = 10 * 60 * 1000L // 10 минут
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        if (logId == -1L) return

        val pendingResult = goAsync()
        val database = AppDatabase.getInstance(context)

        when (intent.action) {
            ACTION_TAKEN -> {
                NotificationHelper.cancelNotification(context, logId.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        database.intakeLogDao().updateStatus(
                            id = logId,
                            status = IntakeStatus.TAKEN,
                            actualTime = System.currentTimeMillis()
                        )
                        if (medicationId != -1L) {
                            database.medicationDao().decrementQuantity(medicationId)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_SNOOZE -> {
                val medName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Лекарство"
                val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
                NotificationHelper.cancelNotification(context, logId.toInt())

                val snoozeTime = System.currentTimeMillis() + SNOOZE_DELAY_MILLIS
                AlarmScheduler.scheduleAlarm(
                    context = context,
                    logId = logId,
                    medicationId = medicationId,
                    timeMillis = snoozeTime,
                    medicationName = medName,
                    dosage = dosage
                )
                pendingResult.finish()
            }
            else -> pendingResult.finish()
        }
    }
}