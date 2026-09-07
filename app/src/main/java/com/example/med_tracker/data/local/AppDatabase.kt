package com.example.med_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.med_tracker.data.local.converter.Converters
import com.example.med_tracker.data.local.dao.IntakeLogDao
import com.example.med_tracker.data.local.dao.MedicationDao
import com.example.med_tracker.data.local.dao.ScheduleDao
import com.example.med_tracker.data.local.entity.IntakeLogEntity
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.data.local.entity.ScheduleEntity

@Database(
    entities = [
        MedicationEntity::class,
        ScheduleEntity::class,
        IntakeLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun intakeLogDao(): IntakeLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medicationId INTEGER NOT NULL,
                        time TEXT NOT NULL,
                        scheduleType TEXT NOT NULL DEFAULT 'DAYS_OF_WEEK',
                        daysOfWeek TEXT NOT NULL DEFAULT '[]',
                        intervalDays INTEGER NOT NULL DEFAULT 1,
                        cycleIntakeDays INTEGER NOT NULL DEFAULT 1,
                        cyclePauseDays INTEGER NOT NULL DEFAULT 0,
                        startDateMillis INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(medicationId) REFERENCES medications(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_schedules_medicationId ON schedules(medicationId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS intake_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medicationId INTEGER NOT NULL,
                        scheduledTimeMillis INTEGER NOT NULL,
                        actualTimeMillis INTEGER,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY(medicationId) REFERENCES medications(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_intake_logs_medicationId ON intake_logs(medicationId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_intake_logs_scheduledTime ON intake_logs(scheduledTimeMillis)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }
    }
}