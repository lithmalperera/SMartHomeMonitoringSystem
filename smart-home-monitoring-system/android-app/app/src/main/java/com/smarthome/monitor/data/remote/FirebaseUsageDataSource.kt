package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.core.util.DateUtils
import com.smarthome.monitor.data.model.UsageRecord
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUsageDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeUsage(): Flow<Map<String, Map<String, UsageRecord>>> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.usageRoot())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usageByDevice = mutableMapOf<String, MutableMap<String, UsageRecord>>()
                for (deviceChild in snapshot.children) {
                    val deviceId = deviceChild.key ?: continue
                    val recordsByDate = mutableMapOf<String, UsageRecord>()
                    for (dateChild in deviceChild.children) {
                        val date = dateChild.key ?: continue
                        val record = dateChild.getValue(UsageRecord::class.java) ?: continue
                        recordsByDate[date] = record.copy(date = date)
                    }
                    usageByDevice[deviceId] = recordsByDate
                }
                trySend(usageByDevice)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun recordUsage(deviceId: String, activeMinutes: Long, energyWh: Double) {
        val ref = db.getReference(Constants.DbPaths.usageDate(deviceId, DateUtils.todayKey()))
        ref.updateChildren(
            mapOf(
                "activeMinutes" to ServerValue.increment(activeMinutes),
                "sessions" to ServerValue.increment(1),
                "energyWh" to ServerValue.increment(energyWh)
            )
        ).await()
    }

    suspend fun recordCutoff(deviceId: String) {
        val ref = db.getReference(Constants.DbPaths.usageDate(deviceId, DateUtils.todayKey()))
        ref.updateChildren(
            mapOf("autoCutoffs" to ServerValue.increment(1))
        ).await()
    }
}
