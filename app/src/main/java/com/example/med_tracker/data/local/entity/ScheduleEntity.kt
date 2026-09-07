package com.example.med_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduleType {
    DAYS_OF_WEEK, // Выбранные дни недели (Пн, Вт...)
    INTERVAL,     // Каждые N дней
    CYCLE         // Цикл: X дней прием / Y дней перерыв (например, 2 через 2)
}

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val time: String, // "HH:mm"
    val scheduleType: ScheduleType = ScheduleType.DAYS_OF_WEEK,
    val daysOfWeek: List<Int> = emptyList(), // 1 = Пн ... 7 = Вс
    val intervalDays: Int = 1,              // Для INTERVAL: каждые N дней
    val cycleIntakeDays: Int = 1,           // Для CYCLE: дней приема подряд
    val cyclePauseDays: Int = 0,            // Для CYCLE: дней пропуска подряд
    val startDateMillis: Long = 0L          // Дата начала отсчета интервала/цикла
)