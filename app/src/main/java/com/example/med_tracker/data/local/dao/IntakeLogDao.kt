package com.example.med_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.med_tracker.data.local.entity.IntakeLogEntity
import com.example.med_tracker.data.local.entity.IntakeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {
    @Query("SELECT * FROM intake_logs WHERE scheduledTimeMillis BETWEEN :startTime AND :endTime ORDER BY scheduledTimeMillis ASC")
    fun getLogsForDay(startTime: Long, endTime: Long): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE scheduledTimeMillis BETWEEN :startTime AND :endTime ORDER BY scheduledTimeMillis DESC")
    fun getHistoryLogsForPeriod(startTime: Long, endTime: Long): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE status = 'PENDING' ORDER BY scheduledTimeMillis ASC")
    fun getUpcomingLogs(): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE status != 'PENDING' ORDER BY scheduledTimeMillis DESC")
    fun getPastLogs(): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE medicationId = :medicationId AND status = 'PENDING' AND scheduledTimeMillis >= :fromTime")
    suspend fun getFuturePendingLogsSync(medicationId: Long, fromTime: Long): List<IntakeLogEntity>

    @Query("DELETE FROM intake_logs WHERE medicationId = :medicationId AND status = 'PENDING' AND scheduledTimeMillis >= :fromTime")
    suspend fun deleteFuturePendingLogs(medicationId: Long, fromTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IntakeLogEntity): Long

    @Query("UPDATE intake_logs SET status = :status, actualTimeMillis = :actualTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: IntakeStatus, actualTime: Long?)

    @Query("SELECT * FROM intake_logs WHERE status != 'PENDING' ORDER BY scheduledTimeMillis DESC")
    fun getAllHistoryLogs(): Flow<List<IntakeLogEntity>>
}