package com.example.med_tracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.med_tracker.presentation.cabinet.MedicationSortOrder
import com.example.med_tracker.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        private val SHOW_QUANTITY_KEY = booleanPreferencesKey("show_quantity_in_intakes")
    }

    val themeMode: Flow<AppThemeMode> = dataStore.data.map { preferences ->
        val mode = preferences[THEME_MODE_KEY] ?: "amoled"
        when (mode) {
            "light" -> AppThemeMode.LIGHT
            "dark" -> AppThemeMode.DARK
            "amoled" -> AppThemeMode.AMOLED
            else -> AppThemeMode.SYSTEM
        }
    }

    val sortOrder: Flow<MedicationSortOrder> = dataStore.data.map { preferences ->
        val order = preferences[SORT_ORDER_KEY] ?: "name_asc"
        when (order) {
            "name_asc" -> MedicationSortOrder.NAME_ASC
            "name_desc" -> MedicationSortOrder.NAME_DESC
            "quantity_asc" -> MedicationSortOrder.QUANTITY_ASC
            "quantity_desc" -> MedicationSortOrder.QUANTITY_DESC
            else -> MedicationSortOrder.NAME_ASC
        }
    }

    val showQuantityInIntakes: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_QUANTITY_KEY] ?: true
    }

    suspend fun saveThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = when (mode) {
                AppThemeMode.LIGHT -> "light"
                AppThemeMode.DARK -> "dark"
                AppThemeMode.AMOLED -> "amoled"
                AppThemeMode.SYSTEM -> "system"
            }
        }
    }

    suspend fun saveSortOrder(order: MedicationSortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = when (order) {
                MedicationSortOrder.NAME_ASC -> "name_asc"
                MedicationSortOrder.NAME_DESC -> "name_desc"
                MedicationSortOrder.QUANTITY_ASC -> "quantity_asc"
                MedicationSortOrder.QUANTITY_DESC -> "quantity_desc"
            }
        }
    }

    suspend fun saveShowQuantityInIntakes(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_QUANTITY_KEY] = show
        }
    }
}