package com.example.med_tracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.dao.IntakeLogDao
import com.example.med_tracker.data.local.dao.MedicationDao
import com.example.med_tracker.data.local.dao.ScheduleDao
import com.example.med_tracker.data.repository.MedicationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext app: Context): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext app: Context): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideMedicationDao(database: AppDatabase): MedicationDao {
        return database.medicationDao()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: AppDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideIntakeLogDao(database: AppDatabase): IntakeLogDao {
        return database.intakeLogDao()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext app: Context): DataStore<Preferences> {
        return app.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsPreferencesRepository(dataStore: DataStore<Preferences>): SettingsPreferencesRepository {
        return SettingsPreferencesRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(
        database: AppDatabase
    ): MedicationRepository {
        return MedicationRepository(database)
    }
}