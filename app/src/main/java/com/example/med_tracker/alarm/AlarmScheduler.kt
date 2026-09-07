
package com.example.med_tracker.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    /**
     * Генерирует уникальный requestCode для PendingIntent.
     * Используется комбинация хеша logId и флага, чтобы гарантировать уникальность
     * и избежать коллизий. Хеш logId безопасен, так как hashCode(Long) эквивалентен
     * toInt(), но мы добавляем отдельный флаг для schedule/cancel, что делает
     * PendingIntent уникальным даже при одинаковых logId.
     */
    private fun requestCodeFor(logId: Long, flag: Int): Int =
        (logId.hashCode() shl 31) xor flag

    fun scheduleAlarm(
        context: Context,
        logId: Long,
        medicationId: Long,
        timeMillis: Long,
        medicationName: String,
        dosage: String,
        notifyBeforeMinutes: Int = 0,
        snoozeMinutes: Int = 10
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerTime = timeMillis - (notifyBeforeMinutes * 60 * 1000L)
        val finalTriggerTime = if (triggerTime < System.currentTimeMillis()) timeMillis else triggerTime

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(NotificationActionReceiver.EXTRA_DOSAGE, dosage)
            putExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(logId, flag = 1),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                finalTriggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                finalTriggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, logId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(logId, flag = 2),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

