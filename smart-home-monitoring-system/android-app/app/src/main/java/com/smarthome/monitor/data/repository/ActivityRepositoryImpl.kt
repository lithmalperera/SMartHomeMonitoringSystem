package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.ActivityType
import com.smarthome.monitor.data.model.RecentActivity
import com.smarthome.monitor.data.remote.FirebaseActivityDataSource
import com.smarthome.monitor.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseActivityDataSource
) : ActivityRepository {
    override fun observeActivities(): Flow<List<RecentActivity>> = dataSource.observeActivities()

    override suspend fun logActivity(title: String, subtitle: String, type: ActivityType) {
        dataSource.logActivity(title, subtitle, type)
    }
}
