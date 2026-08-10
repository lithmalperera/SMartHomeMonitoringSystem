package com.smarthome.monitor.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smarthome.monitor.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val IRON_LIMIT_MINUTES = intPreferencesKey("iron_limit_minutes")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    override val notificationsEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    override val ironLimitMinutes: Flow<Int> =
        context.settingsDataStore.data.map { it[Keys.IRON_LIMIT_MINUTES] ?: 15 }

    override val userName: Flow<String> =
        context.settingsDataStore.data.map { it[Keys.USER_NAME] ?: "Videesha" }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setIronLimitMinutes(minutes: Int) {
        context.settingsDataStore.edit { it[Keys.IRON_LIMIT_MINUTES] = minutes }
    }

    override suspend fun setUserName(name: String) {
        context.settingsDataStore.edit { it[Keys.USER_NAME] = name }
    }
}
