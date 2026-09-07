package com.example.med_tracker.data.repository

import android.content.Context
import com.example.med_tracker.alarm.AlarmScheduler
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.IntakeLogEntity
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.data.local.entity.ScheduleEntity
import com.example.med_tracker.data.local.entity.ScheduleType
import com.example.med_tracker.domain.model.TodayIntakeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MedicationRepository(database: AppDatabase) {

    private val medicationDao = database.medicationDao()
    private val scheduleDao = database.scheduleDao()
    private val intakeLogDao = database.intakeLogDao()

    val allMedications: Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    suspend fun addMedicationWithSchedules(
        medication: MedicationEntity,
        times: List<String>,
        scheduleType: ScheduleType,
        daysOfWeek: List<Int>,
        intervalDays: Int,
        cycleIntakeDays: Int,
        cyclePauseDays: Int,
        context: Context
    ) {
        val medId = medicationDao.insertMedication(medication)
        val todayMidnight = getStartOfDay(System.currentTimeMillis())

        times.forEach { time ->
            scheduleDao.insertSchedule(
                ScheduleEntity(
                    medicationId = medId,
                    time = time,
                    scheduleType = scheduleType,
                    daysOfWeek = daysOfWeek,
                    intervalDays = intervalDays,
                    cycleIntakeDays = cycleIntakeDays,
                    cyclePauseDays = cyclePauseDays,
                    startDateMillis = todayMidnight
                )
            )
        }
        generateLogsAhead(context, daysAhead = 14)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.deleteMedication(medication)
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.update(medication)
    }

    suspend fun updateMedicationWithSchedules(
        medication: MedicationEntity,
        times: List<String>,
        scheduleType: ScheduleType,
        daysOfWeek: List<Int>,
        intervalDays: Int,
        cycleIntakeDays: Int,
        cyclePauseDays: Int,
        context: Context
    ) {
        medicationDao.update(medication)

        val oldSchedules = scheduleDao.getSchedulesForMedicationSync(medication.id)
        oldSchedules.forEach { scheduleDao.deleteSchedule(it) }

        val todayMidnight = getStartOfDay(System.currentTimeMillis())
        times.forEach { time ->
            scheduleDao.insertSchedule(
                ScheduleEntity(
                    medicationId = medication.id,
                    time = time,
                    scheduleType = scheduleType,
                    daysOfWeek = daysOfWeek,
                    intervalDays = intervalDays,
                    cycleIntakeDays = cycleIntakeDays,
                    cyclePauseDays = cyclePauseDays,
                    startDateMillis = todayMidnight
                )
            )
        }

        generateLogsAhead(context, daysAhead = 14)
    }

    suspend fun getScheduleTimesForMedication(medicationId: Long): List<String> {
        return scheduleDao.getSchedulesForMedicationSync(medicationId).map { it.time }
    }

    fun getUpcomingIntakes(): Flow<List<TodayIntakeItem>> {
        return combine(
            intakeLogDao.getUpcomingLogs(),
            medicationDao.getAllMedications()
        ) { logs, medications ->
            mapToItems(logs, medications)
        }
    }

    fun getPastIntakes(): Flow<List<TodayIntakeItem>> {
        return combine(
            intakeLogDao.getPastLogs(),
            medicationDao.getAllMedications()
        ) { logs, medications ->
            mapToItems(logs, medications)
        }
    }

    fun getHistoryLogs(startTimeMillis: Long, endTimeMillis: Long): Flow<List<TodayIntakeItem>> {
        return combine(
            intakeLogDao.getHistoryLogsForPeriod(startTimeMillis, endTimeMillis),
            medicationDao.getAllMedications()
        ) { logs, medications ->
            mapToItems(logs, medications)
        }
    }

    private fun mapToItems(
        logs: List<IntakeLogEntity>,
        medications: List<MedicationEntity>
    ): List<TodayIntakeItem> {
        val medMap = medications.associateBy { it.id }
        return logs.mapNotNull { log ->
            val med = medMap[log.medicationId] ?: return@mapNotNull null
            TodayIntakeItem(
                logId = log.id,
                medicationId = med.id,
                name = med.name,
                dosage = med.dosage,
                form = med.form,
                scheduledTimeMillis = log.scheduledTimeMillis,
                actualTimeMillis = log.actualTimeMillis,
                status = log.status,
                remainingQuantity = med.remainingQuantity,
                unit = med.unit,
                notifyBeforeMinutes = med.notifyBeforeMinutes,
                snoozeMinutes = med.snoozeMinutes
            )
        }
    }

    suspend fun updateIntakeStatus(
        context: Context,
        item: TodayIntakeItem,
        newStatus: IntakeStatus,
        actualTimeMillis: Long?
    ) {
        intakeLogDao.updateStatus(item.logId, newStatus, actualTimeMillis)
        if (newStatus == IntakeStatus.TAKEN && item.status != IntakeStatus.TAKEN) {
            medicationDao.decrementQuantity(item.medicationId)
        }
        AlarmScheduler.cancelAlarm(context, item.logId)
    }

    suspend fun generateLogsAhead(context: Context, daysAhead: Int = 14) {
        val allSchedules = scheduleDao.getAllSchedulesSync()
        val medications = medicationDao.getAllMedications().first().associateBy { it.id }

        for (offset in 0..daysAhead) {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val startOfDay = getStartOfDay(calendar.timeInMillis)
            val endOfDay = getEndOfDay(calendar.timeInMillis)
            val existingLogs = intakeLogDao.getLogsForDay(startOfDay, endOfDay).first()

            val currentDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            for (schedule in allSchedules) {
                val isScheduled = when (schedule.scheduleType) {
                    ScheduleType.DAYS_OF_WEEK -> schedule.daysOfWeek.contains(currentDayOfWeek)
                    ScheduleType.INTERVAL -> {
                        val daysDiff = TimeUnit.MILLISECONDS.toDays(startOfDay - schedule.startDateMillis).toInt()
                        daysDiff >= 0 && (daysDiff % schedule.intervalDays == 0)
                    }
                    ScheduleType.CYCLE -> {
                        val daysDiff = TimeUnit.MILLISECONDS.toDays(startOfDay - schedule.startDateMillis).toInt()
                        val totalCycle = schedule.cycleIntakeDays + schedule.cyclePauseDays
                        if (daysDiff >= 0 && totalCycle > 0) {
                            (daysDiff % totalCycle) < schedule.cycleIntakeDays
                        } else false
                    }
                }

                if (isScheduled) {
                    val timeParts = schedule.time.split(":")
                    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: continue
                    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: continue

                    val logCalendar = Calendar.getInstance().apply {
                        timeInMillis = calendar.timeInMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val scheduledMillis = logCalendar.timeInMillis

                    val exists = existingLogs.any {
                        it.medicationId == schedule.medicationId && it.scheduledTimeMillis == scheduledMillis
                    }

                    if (!exists) {
                        val logId = intakeLogDao.insertLog(
                            IntakeLogEntity(
                                medicationId = schedule.medicationId,
                                scheduledTimeMillis = scheduledMillis,
                                status = IntakeStatus.PENDING
                            )
                        )

                        if (scheduledMillis > System.currentTimeMillis()) {
                            val med = medications[schedule.medicationId]
                            AlarmScheduler.scheduleAlarm(
                                context = context,
                                logId = logId,
                                medicationId = schedule.medicationId,
                                timeMillis = scheduledMillis,
                                medicationName = med?.name ?: "Препарат",
                                dosage = med?.dosage ?: "",
                                notifyBeforeMinutes = med?.notifyBeforeMinutes ?: 0,
                                snoozeMinutes = med?.snoozeMinutes ?: 10
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getStartOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}