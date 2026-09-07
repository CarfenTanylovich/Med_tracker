package com.example.med_tracker.domain.model

import com.example.med_tracker.data.local.entity.IntakeStatus

data class TodayIntakeItem(
    val logId: Long,
    val medicationId: Long,
    val name: String,
    val dosage: String,
    val form: String,
    val scheduledTimeMillis: Long,
    val actualTimeMillis: Long?,
    val status: IntakeStatus,
    val remainingQuantity: Int,
    val unit: String,
    val notifyBeforeMinutes: Int = 0,
    val snoozeMinutes: Int = 10
)