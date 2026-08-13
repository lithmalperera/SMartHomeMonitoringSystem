package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.Schedule
import com.smarthome.monitor.data.remote.FirebaseScheduleDataSource
import com.smarthome.monitor.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseScheduleDataSource
) : ScheduleRepository {

    override fun observeSchedules(): Flow<List<Schedule>> = dataSource.observeSchedules()

    override suspend fun updateLastRunKey(scheduleId: String, lastRunKey: String) {
        dataSource.updateLastRunKey(scheduleId, lastRunKey)
    }
}
