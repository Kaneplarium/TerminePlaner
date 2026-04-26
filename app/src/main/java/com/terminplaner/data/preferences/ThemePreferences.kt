package com.terminplaner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
        val PROFILE_PICTURE_URI_KEY = stringPreferencesKey("profile_picture_uri")
        
        val PERSONAL_ADDRESS_KEY = stringPreferencesKey("personal_address")
        val PERSONAL_BIRTHDAY_KEY = longPreferencesKey("personal_birthday")

        val EMPLOYER_NAME_KEY = stringPreferencesKey("employer_name")
        val EMPLOYER_ADDRESS_KEY = stringPreferencesKey("employer_address")
        val EMPLOYER_PLZ_KEY = stringPreferencesKey("employer_plz")
        val EMPLOYER_CITY_KEY = stringPreferencesKey("employer_city")
        val STORAGE_PATH_KEY = stringPreferencesKey("storage_path")
        val FIRST_RUN_COMPLETED_KEY = booleanPreferencesKey("first_run_completed")
        val TRASH_AUTO_DELETE_DAYS_KEY = intPreferencesKey("trash_auto_delete_days")
        val DELETE_LINKED_TASKS_KEY = booleanPreferencesKey("delete_linked_tasks")
        val USER_STATUS_KEY = intPreferencesKey("user_status")
        val FOCUS_FILTER_KEY = intPreferencesKey("focus_filter")
        val SMARTWATCH_SYNC_KEY = booleanPreferencesKey("smartwatch_sync")
        val CALENDAR_VISIBILITY_KEY = intPreferencesKey("calendar_visibility")

        // Missing keys restored
        val SHOW_WEEKEND_KEY = booleanPreferencesKey("show_weekend")
        val SHOW_HOLIDAYS_KEY = booleanPreferencesKey("show_holidays")
        val TRAVEL_TIME_KEY = booleanPreferencesKey("travel_time")
        val DEFAULT_REMINDER_KEY = intPreferencesKey("default_reminder")
        val IMPORT_BIRTHDAYS_KEY = booleanPreferencesKey("import_birthdays")
        val ALLOW_INVITATIONS_KEY = booleanPreferencesKey("allow_invitations")
        val BUSINESS_RECURRING_APPOINTMENTS_KEY = booleanPreferencesKey("biz_recurring")
        val BUSINESS_CONFIRMATIONS_KEY = booleanPreferencesKey("biz_confirmations")
        
        private const val DEFAULT_COLOR = 0xFF007AFF // Blue
        const val MODE_SYSTEM = 0
        const val MODE_LIGHT = 1
        const val MODE_DARK = 2
        
        const val STATUS_NONE = 0
        const val STATUS_PERSONAL = 1
        const val STATUS_BUSINESS = 2

        const val FILTER_ALL = 0
        const val FILTER_BUSINESS = 1
        const val FILTER_PERSONAL = 2
        
        const val VISIBILITY_PRIVATE = 0
        const val VISIBILITY_FAMILY = 1
    }

    val themeColor: Flow<Long> = context.dataStore.data.map { it[THEME_COLOR_KEY] ?: DEFAULT_COLOR }
    val darkThemeMode: Flow<Int> = context.dataStore.data.map { it[DARK_MODE_KEY] ?: MODE_SYSTEM }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR_KEY] ?: true }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val profilePictureUri: Flow<String?> = context.dataStore.data.map { it[PROFILE_PICTURE_URI_KEY] }
    
    val personalAddress: Flow<String?> = context.dataStore.data.map { it[PERSONAL_ADDRESS_KEY] }
    val personalBirthday: Flow<Long?> = context.dataStore.data.map { it[PERSONAL_BIRTHDAY_KEY] }

    val employerName: Flow<String?> = context.dataStore.data.map { it[EMPLOYER_NAME_KEY] }
    val employerAddress: Flow<String?> = context.dataStore.data.map { it[EMPLOYER_ADDRESS_KEY] }
    val employerPLZ: Flow<String?> = context.dataStore.data.map { it[EMPLOYER_PLZ_KEY] }
    val employerCity: Flow<String?> = context.dataStore.data.map { it[EMPLOYER_CITY_KEY] }

    val storagePath: Flow<String?> = context.dataStore.data.map { it[STORAGE_PATH_KEY] }
    val isFirstRunCompleted: Flow<Boolean> = context.dataStore.data.map { it[FIRST_RUN_COMPLETED_KEY] ?: false }
    val trashAutoDeleteDays: Flow<Int> = context.dataStore.data.map { it[TRASH_AUTO_DELETE_DAYS_KEY] ?: 30 }
    val deleteLinkedTasks: Flow<Boolean> = context.dataStore.data.map { it[DELETE_LINKED_TASKS_KEY] ?: true }
    val userStatus: Flow<Int> = context.dataStore.data.map { it[USER_STATUS_KEY] ?: STATUS_NONE }
    val focusFilter: Flow<Int> = context.dataStore.data.map { it[FOCUS_FILTER_KEY] ?: FILTER_ALL }
    val smartwatchSync: Flow<Boolean> = context.dataStore.data.map { it[SMARTWATCH_SYNC_KEY] ?: false }
    val calendarVisibility: Flow<Int> = context.dataStore.data.map { it[CALENDAR_VISIBILITY_KEY] ?: VISIBILITY_PRIVATE }

    // Personal Flows
    val showWeekend: Flow<Boolean> = context.dataStore.data.map { it[SHOW_WEEKEND_KEY] ?: true }
    val showHolidays: Flow<Boolean> = context.dataStore.data.map { it[SHOW_HOLIDAYS_KEY] ?: true }
    val travelTime: Flow<Boolean> = context.dataStore.data.map { it[TRAVEL_TIME_KEY] ?: false }
    val defaultReminder: Flow<Int> = context.dataStore.data.map { it[DEFAULT_REMINDER_KEY] ?: 15 }
    val importBirthdays: Flow<Boolean> = context.dataStore.data.map { it[IMPORT_BIRTHDAYS_KEY] ?: false }
    val allowInvitations: Flow<Boolean> = context.dataStore.data.map { it[ALLOW_INVITATIONS_KEY] ?: true }

    // Business Flows
    val bizRecurring: Flow<Boolean> = context.dataStore.data.map { it[BUSINESS_RECURRING_APPOINTMENTS_KEY] ?: false }
    val bizConfirmations: Flow<Boolean> = context.dataStore.data.map { it[BUSINESS_CONFIRMATIONS_KEY] ?: true }

    suspend fun setThemeColor(color: Long) = context.dataStore.edit { it[THEME_COLOR_KEY] = color }
    suspend fun setDarkMode(mode: Int) = context.dataStore.edit { it[DARK_MODE_KEY] = mode }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[DYNAMIC_COLOR_KEY] = enabled }
    suspend fun setUserName(name: String?) = context.dataStore.edit { if (name == null) it.remove(USER_NAME_KEY) else it[USER_NAME_KEY] = name }
    suspend fun setProfilePictureUri(uri: String?) = context.dataStore.edit { if (uri == null) it.remove(PROFILE_PICTURE_URI_KEY) else it[PROFILE_PICTURE_URI_KEY] = uri }
    
    suspend fun setPersonalData(address: String?, birthday: Long?) = context.dataStore.edit {
        if (address == null) it.remove(PERSONAL_ADDRESS_KEY) else it[PERSONAL_ADDRESS_KEY] = address
        if (birthday == null) it.remove(PERSONAL_BIRTHDAY_KEY) else it[PERSONAL_BIRTHDAY_KEY] = birthday
    }

    suspend fun setEmployerData(name: String?, address: String?, plz: String?, city: String?) = context.dataStore.edit {
        if (name == null) it.remove(EMPLOYER_NAME_KEY) else it[EMPLOYER_NAME_KEY] = name
        if (address == null) it.remove(EMPLOYER_ADDRESS_KEY) else it[EMPLOYER_ADDRESS_KEY] = address
        if (plz == null) it.remove(EMPLOYER_PLZ_KEY) else it[EMPLOYER_PLZ_KEY] = plz
        if (city == null) it.remove(EMPLOYER_CITY_KEY) else it[EMPLOYER_CITY_KEY] = city
    }

    suspend fun setStoragePath(path: String?) = context.dataStore.edit { if (path == null) it.remove(STORAGE_PATH_KEY) else it[STORAGE_PATH_KEY] = path }
    suspend fun setTrashAutoDeleteDays(days: Int) = context.dataStore.edit { it[TRASH_AUTO_DELETE_DAYS_KEY] = days }
    suspend fun setDeleteLinkedTasks(enabled: Boolean) = context.dataStore.edit { it[DELETE_LINKED_TASKS_KEY] = enabled }
    suspend fun setFirstRunCompleted() = context.dataStore.edit { it[FIRST_RUN_COMPLETED_KEY] = true }
    suspend fun setUserStatus(status: Int) = context.dataStore.edit { it[USER_STATUS_KEY] = status }
    suspend fun setFocusFilter(filter: Int) = context.dataStore.edit { it[FOCUS_FILTER_KEY] = filter }
    suspend fun setSmartwatchSync(enabled: Boolean) = context.dataStore.edit { it[SMARTWATCH_SYNC_KEY] = enabled }
    suspend fun setCalendarVisibility(visibility: Int) = context.dataStore.edit { it[CALENDAR_VISIBILITY_KEY] = visibility }

    // Personal Setters
    suspend fun setShowWeekend(enabled: Boolean) = context.dataStore.edit { it[SHOW_WEEKEND_KEY] = enabled }
    suspend fun setShowHolidays(enabled: Boolean) = context.dataStore.edit { it[SHOW_HOLIDAYS_KEY] = enabled }
    suspend fun setTravelTime(enabled: Boolean) = context.dataStore.edit { it[TRAVEL_TIME_KEY] = enabled }
    suspend fun setDefaultReminder(minutes: Int) = context.dataStore.edit { it[DEFAULT_REMINDER_KEY] = minutes }
    suspend fun setImportBirthdays(enabled: Boolean) = context.dataStore.edit { it[IMPORT_BIRTHDAYS_KEY] = enabled }
    suspend fun setAllowInvitations(enabled: Boolean) = context.dataStore.edit { it[ALLOW_INVITATIONS_KEY] = enabled }

    // Business Setters
    suspend fun setBizRecurring(enabled: Boolean) = context.dataStore.edit { it[BUSINESS_RECURRING_APPOINTMENTS_KEY] = enabled }
    suspend fun setBizConfirmations(enabled: Boolean) = context.dataStore.edit { it[BUSINESS_CONFIRMATIONS_KEY] = enabled }
}
