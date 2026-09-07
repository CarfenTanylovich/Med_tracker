package com.example.med_tracker.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.IntakeLogEntity
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.local.entity.MedicationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                restoreAlarms(context)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to restore alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun restoreAlarms(context: Context) {
        val database = AppDatabase.getInstance(context)
        val now = System.currentTimeMillis()
        val windowEnd = now + TimeUnit.HOURS.toMillis(48)

        val allLogs: List<IntakeLogEntity> = database.intakeLogDao().getUpcomingLogs().first()
        val pendingLogs = allLogs.filter { log: IntakeLogEntity ->
            log.status == IntakeStatus.PENDING && log.scheduledTimeMillis in (now + 1)..windowEnd
        }

        val allMedications: List<MedicationEntity> = database.medicationDao().getAllMedications().first()
        val medMap = allMedications.associateBy { medication: MedicationEntity -> medication.id }

        for (log in pendingLogs) {
            val medication = medMap[log.medicationId]
            if (medication != null) {
                withContext(Dispatchers.Main) {
                    AlarmScheduler.scheduleAlarm(
                        context = context,
                        logId = log.id,
                        medicationId = log.medicationId,
                        timeMillis = log.scheduledTimeMillis,
                        medicationName = medication.name,
                        dosage = medication.dosage,
                        notifyBeforeMinutes = medication.notifyBeforeMinutes,
                        snoozeMinutes = medication.snoozeMinutes
                    )
                }
            }
        }
    }
}