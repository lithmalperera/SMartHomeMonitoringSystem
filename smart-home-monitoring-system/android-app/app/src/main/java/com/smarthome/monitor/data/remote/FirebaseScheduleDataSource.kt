package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.data.model.Schedule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseScheduleDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeSchedules(): Flow<List<Schedule>> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.schedules())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val schedules = mutableListOf<Schedule>()
                for (child in snapshot.children) {
                    val schedule = child.getValue(Schedule::class.java)
                    if (schedule != null) {
                        schedules.add(schedule.copy(id = child.key ?: ""))
                    }
                }
                trySend(schedules)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateLastRunKey(scheduleId: String, lastRunKey: String) {
        db.getReference(Constants.DbPaths.schedule(scheduleId))
            .child("lastRunKey")
            .setValue(lastRunKey)
            .await()
    }
}
