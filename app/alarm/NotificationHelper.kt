package com.example.med_tracker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "medication_reminders_channel"
    private const val CHANNEL_NAME = "Напоминания о приеме лекарств"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для уведомлений о необходимости принять лекарство"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showIntakeNotification(
        context: Context,
        logId: Long,
        medicationId: Long,
        medicationName: String,
        dosage: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Кнопка «Принял»
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKEN
            putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (logId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка «Отложить» (на 10 минут)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(NotificationActionReceiver.EXTRA_DOSAGE, dosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (logId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Время принять лекарство: $medicationName")
            .setContentText("Дозировка: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Принял", takenPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Отложить", snoozePendingIntent)
            .build()

        manager.notify(logId.toInt(), notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}