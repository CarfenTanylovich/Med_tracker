package com.example.med_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.med_tracker.data.local.entity.ScheduleEntity

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedulesSync(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId")
    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<ScheduleEntity>

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}