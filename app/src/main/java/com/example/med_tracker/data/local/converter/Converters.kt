package com.example.med_tracker.data.local.converter

import androidx.room.TypeConverter
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.local.entity.ScheduleType

class Converters {
    @TypeConverter
    fun fromDaysList(days: List<Int>?): String {
        return days?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toDaysList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromStatus(status: IntakeStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): IntakeStatus {
        return runCatching { IntakeStatus.valueOf(value) }.getOrDefault(IntakeStatus.PENDING)
    }

    @TypeConverter
    fun fromScheduleType(type: ScheduleType): String {
        return type.name
    }

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType {
        return runCatching { ScheduleType.valueOf(value) }.getOrDefault(ScheduleType.DAYS_OF_WEEK)
    }
}