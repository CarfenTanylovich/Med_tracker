package com.example.med_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_tracker_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}