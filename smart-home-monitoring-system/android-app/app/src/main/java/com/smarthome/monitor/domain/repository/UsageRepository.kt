package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.UsageRecord
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun observeUsage(): Flow<Map<String, Map<String, UsageRecord>>>
}
