package com.terminplaner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    
    companion object {
        val THEME_COLOR_KEY = longPreferencesKey("theme_color")
        private const val DEFAULT_COLOR = 0xFF2196F3 // Blue
    }

    val themeColor: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[THEME_COLOR_KEY] ?: DEFAULT_COLOR
    }

    suspend fun setThemeColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color
        }
    }
}
