package com.smarthome.monitor.data.repository

import com.smarthome.monitor.data.model.UsageRecord
import com.smarthome.monitor.data.remote.FirebaseUsageDataSource
import com.smarthome.monitor.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseUsageDataSource
) : UsageRepository {

    override fun observeUsage(): Flow<Map<String, Map<String, UsageRecord>>> =
        dataSource.observeUsage()
}
