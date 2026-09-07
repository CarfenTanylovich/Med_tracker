package com.example.med_tracker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.med_tracker.MainActivity
import com.example.med_tracker.R

object NotificationHelper {

    private const val CHANNEL_ID = "medication_reminders_channel"
    private const val CHANNEL_NAME = "Напоминания о приеме лекарств"

    fun notificationIdFor(logId: Long): Int = (logId % 100_000L).toInt()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для напоминаний о приеме лекарств"
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
        dosage: String,
        snoozeMinutes: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context)

        val notificationId = notificationIdFor(logId)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка: Принял
        val takeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKE
            putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка: Отложить
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(NotificationActionReceiver.EXTRA_DOSAGE, dosage)
            putExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка: Пропустить
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_form_tablet)
            .setContentTitle("Время принять: $medicationName")
            .setContentText("Дозировка: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Принял", takePendingIntent)
            .addAction(0, "Отложить ($snoozeMinutes мин)", snoozePendingIntent)
            .addAction(0, "Пропустить", skipPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, logId: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationIdFor(logId))
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}