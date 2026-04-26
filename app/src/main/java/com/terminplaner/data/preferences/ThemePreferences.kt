package com.terminplaner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemePreferences @Inject constructor(@param:ApplicationContext private val context: Context) {
    
    companion object {
        val THEME_COLOR_KEY = longPreferencesKey("theme_color")
        val DARK_MODE_KEY = intPreferencesKey("dark_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val EMPLOYER_KEY = stringPreferencesKey("employer")
        val STORAGE_PATH_KEY = stringPreferencesKey("storage_path")
        val FIRST_RUN_COMPLETED_KEY = booleanPreferencesKey("first_run_completed")
        val TRASH_AUTO_DELETE_DAYS_KEY = intPreferencesKey("trash_auto_delete_days")
        val DELETE_LINKED_TASKS_KEY = booleanPreferencesKey("delete_linked_tasks")
        val USER_STATUS_KEY = intPreferencesKey("user_status")
        val SMARTWATCH_SYNC_KEY = booleanPreferencesKey("smartwatch_sync")
        
        private const val DEFAULT_COLOR = 0xFFE53935 // Red
        const val MODE_SYSTEM = 0
        const val MODE_LIGHT = 1
        const val MODE_DARK = 2
        
        const val STATUS_NONE = 0
        const val STATUS_PRO = 1
        const val STATUS_BUSINESS = 2
    }

    val themeColor: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[THEME_COLOR_KEY] ?: DEFAULT_COLOR
    }

    val darkThemeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: MODE_SYSTEM
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val employer: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMPLOYER_KEY]
    }

    val storagePath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[STORAGE_PATH_KEY]
    }

    val isFirstRunCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_RUN_COMPLETED_KEY] ?: false
    }

    val trashAutoDeleteDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TRASH_AUTO_DELETE_DAYS_KEY] ?: 30
    }

    val deleteLinkedTasks: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DELETE_LINKED_TASKS_KEY] ?: true
    }

    val userStatus: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_STATUS_KEY] ?: STATUS_NONE
    }

    val smartwatchSync: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SMARTWATCH_SYNC_KEY] ?: false
    }

    suspend fun setThemeColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color
        }
    }

    suspend fun setDarkMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = mode
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setUserName(name: String?) {
        context.dataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(USER_NAME_KEY)
            } else {
                preferences[USER_NAME_KEY] = name
            }
        }
    }

    suspend fun setEmployer(name: String?) {
        context.dataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(EMPLOYER_KEY)
            } else {
                preferences[EMPLOYER_KEY] = name
            }
        }
    }

    suspend fun setStoragePath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(STORAGE_PATH_KEY)
            } else {
                preferences[STORAGE_PATH_KEY] = path
            }
        }
    }

    suspend fun setTrashAutoDeleteDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[TRASH_AUTO_DELETE_DAYS_KEY] = days
        }
    }

    suspend fun setDeleteLinkedTasks(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DELETE_LINKED_TASKS_KEY] = enabled
        }
    }

    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_RUN_COMPLETED_KEY] = true
        }
    }

    suspend fun setUserStatus(status: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_STATUS_KEY] = status
        }
    }

    suspend fun setSmartwatchSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMARTWATCH_SYNC_KEY] = enabled
        }
    }
}
