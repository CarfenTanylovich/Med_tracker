package com.example.med_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val form: String,
    val dosage: String,
    val remainingQuantity: Int,
    val unit: String,
    val notifyBeforeMinutes: Int = 0, // 0 = вовремя, 5, 10, 15, 30 мин
    val snoozeMinutes: Int = 10       // 5, 10, 15, 30 мин
)
