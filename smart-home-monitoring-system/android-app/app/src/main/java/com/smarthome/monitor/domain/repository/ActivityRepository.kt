package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.ActivityType
import com.smarthome.monitor.data.model.RecentActivity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeActivities(): Flow<List<RecentActivity>>
    suspend fun logActivity(title: String, subtitle: String, type: ActivityType)
}
