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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MedicationRepository(database: AppDatabase) {

    private val medicationDao = database.medicationDao()
    private val scheduleDao = database.scheduleDao()
    private val intakeLogDao = database.intakeLogDao()

    val allMedications: Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    private fun getStartOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getDaysBetween(startMillis: Long, endMillis: Long): Long {
        val startOfDay = getStartOfDay(startMillis)
        val endOfDay = getStartOfDay(endMillis)
        val diff = endOfDay - startOfDay
        return diff / (24 * 60 * 60 * 1000L)
    }

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

    suspend fun deleteMedication(medication: MedicationEntity, context: Context) {
        val upcomingLogs = intakeLogDao.getUpcomingLogs().first()
        val medicationLogs = upcomingLogs.filter { it.medicationId == medication.id }
        medicationLogs.forEach { log ->
            AlarmScheduler.cancelAlarm(context, log.id)
        }
        medicationDao.deleteMedication(medication)
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.update(medication)
    }

    suspend fun updateMedicationQuantity(id: Long, quantity: Int) {
        medicationDao.updateQuantity(id, quantity)
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

        val now = System.currentTimeMillis()
        val futurePendingLogs = intakeLogDao.getFuturePendingLogsSync(medication.id, now)
        futurePendingLogs.forEach { log ->
            AlarmScheduler.cancelAlarm(context, log.id)
        }
        intakeLogDao.deleteFuturePendingLogs(medication.id, now)

        val oldSchedules = scheduleDao.getSchedulesForMedicationSync(medication.id)
        oldSchedules.forEach { scheduleDao.deleteSchedule(it) }

        val todayMidnight = getStartOfDay(now)
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

    suspend fun getSchedulesForMedication(medicationId: Long): List<ScheduleEntity> {
        return scheduleDao.getSchedulesForMedicationSync(medicationId)
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

    fun getHistoryLogsFlow(days: Int): Flow<List<TodayIntakeItem>> {
        val calendar = Calendar.getInstance()
        val endTimeMillis = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startTimeMillis = calendar.timeInMillis
        return getHistoryLogs(startTimeMillis, endTimeMillis)
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
        } else if (item.status == IntakeStatus.TAKEN && newStatus != IntakeStatus.TAKEN) {
            medicationDao.updateQuantity(item.medicationId, item.remainingQuantity + 1)
        }

        AlarmScheduler.cancelAlarm(context, item.logId)
    }

    suspend fun generateLogsAhead(context: Context, daysAhead: Int = 14) {
        withContext(Dispatchers.IO) {
            generateLogsInDatabase(daysAhead)
            scheduleUpcomingAlarms(context)
        }
    }

    private suspend fun generateLogsInDatabase(daysAhead: Int) {
        val allSchedules = scheduleDao.getAllSchedulesSync()

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
                        val daysSinceStart = getDaysBetween(schedule.startDateMillis, calendar.timeInMillis)
                        daysSinceStart >= 0 && daysSinceStart % schedule.intervalDays == 0L
                    }
                    ScheduleType.CYCLE -> {
                        val daysSinceStart = getDaysBetween(schedule.startDateMillis, calendar.timeInMillis)
                        if (daysSinceStart < 0) false
                        else {
                            val cycleLength = schedule.cycleIntakeDays + schedule.cyclePauseDays
                            val dayInCycle = (daysSinceStart % cycleLength).toInt()
                            dayInCycle < schedule.cycleIntakeDays
                        }
                    }
                }

                if (isScheduled) {
                    val logCalendar = Calendar.getInstance().apply {
                        timeInMillis = startOfDay
                        val parts = schedule.time.split(":")
                        set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                        set(Calendar.MINUTE, parts[1].toInt())
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val scheduledMillis = logCalendar.timeInMillis

                    val exists = existingLogs.any {
                        it.medicationId == schedule.medicationId && it.scheduledTimeMillis == scheduledMillis
                    }

                    if (!exists) {
                        intakeLogDao.insertLog(
                            IntakeLogEntity(
                                medicationId = schedule.medicationId,
                                scheduledTimeMillis = scheduledMillis,
                                status = IntakeStatus.PENDING
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun scheduleUpcomingAlarms(context: Context) {
        val now = System.currentTimeMillis()
        val windowEnd = now + TimeUnit.HOURS.toMillis(48)

        val upcomingLogs = intakeLogDao.getUpcomingLogs().first()
        val pendingInWindow = upcomingLogs.filter {
            it.status == IntakeStatus.PENDING && it.scheduledTimeMillis in (now + 1)..windowEnd
        }
        val medications = medicationDao.getAllMedications().first().associateBy { it.id }

        for (log in pendingInWindow) {
            val med = medications[log.medicationId] ?: continue
            AlarmScheduler.scheduleAlarm(
                context = context,
                logId = log.id,
                medicationId = log.medicationId,
                timeMillis = log.scheduledTimeMillis,
                medicationName = med.name,
                dosage = med.dosage,
                notifyBeforeMinutes = med.notifyBeforeMinutes,
                snoozeMinutes = med.snoozeMinutes
            )
        }
    }
}