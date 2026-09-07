package com.example.med_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class IntakeStatus {
    PENDING,
    TAKEN,
    MISSED
}

@Entity(
    tableName = "intake_logs",
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
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val scheduledTimeMillis: Long,
    val actualTimeMillis: Long? = null,
    val status: IntakeStatus = IntakeStatus.PENDING
)