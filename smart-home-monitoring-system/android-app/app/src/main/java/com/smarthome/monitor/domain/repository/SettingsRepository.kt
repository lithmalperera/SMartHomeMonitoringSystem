package com.smarthome.monitor.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val notificationsEnabled: Flow<Boolean>
    val ironLimitMinutes: Flow<Int>
    val userName: Flow<String>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setIronLimitMinutes(minutes: Int)
    suspend fun setUserName(name: String)
}
