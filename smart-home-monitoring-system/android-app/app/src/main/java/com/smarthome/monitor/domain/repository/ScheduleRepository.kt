package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeSchedules(): Flow<List<Schedule>>
    suspend fun updateLastRunKey(scheduleId: String, lastRunKey: String)
}
