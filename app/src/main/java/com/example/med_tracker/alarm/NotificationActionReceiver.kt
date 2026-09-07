package com.example.med_tracker.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.IntakeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKE = "com.example.med_tracker.ACTION_TAKE"
        const val ACTION_SNOOZE = "com.example.med_tracker.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.example.med_tracker.ACTION_SKIP"

        const val EXTRA_LOG_ID = "extra_log_id"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        if (logId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getInstance(context)

            try {
                when (intent.action) {
                    ACTION_TAKE -> handleTake(context, database, logId, medicationId)
                    ACTION_SNOOZE -> handleSnooze(context, logId, medicationId, intent)
                    ACTION_SKIP -> handleSkip(context, database, logId)
                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e("NotificationActionReceiver", "Error handling notification action", e)
            } finally {
                withContext(Dispatchers.Main) {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun handleTake(
        context: Context,
        database: AppDatabase,
        logId: Long,
        medicationId: Long
    ) {
        withContext(Dispatchers.Main) {
            NotificationHelper.cancelNotification(context, logId)
        }

        database.intakeLogDao().updateStatus(
            id = logId,
            status = IntakeStatus.TAKEN,
            actualTime = System.currentTimeMillis()
        )

        if (medicationId != -1L) {
            database.medicationDao().decrementQuantity(medicationId)
        }
    }

    private suspend fun handleSnooze(
        context: Context,
        logId: Long,
        medicationId: Long,
        intent: Intent
    ) {
        val medName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Лекарство"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)

        withContext(Dispatchers.Main) {
            NotificationHelper.cancelNotification(context, logId)
        }

        val snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        withContext(Dispatchers.Main) {
            AlarmScheduler.scheduleAlarm(
                context = context,
                logId = logId,
                medicationId = medicationId,
                timeMillis = snoozeTime,
                medicationName = medName,
                dosage = dosage,
                notifyBeforeMinutes = 0,
                snoozeMinutes = snoozeMinutes
            )
        }
    }

    private suspend fun handleSkip(context: Context, database: AppDatabase, logId: Long) {
        withContext(Dispatchers.Main) {
            NotificationHelper.cancelNotification(context, logId)
        }
        database.intakeLogDao().updateStatus(
            id = logId,
            status = IntakeStatus.MISSED,
            actualTime = null
        )
    }
}