package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.data.model.ActivityType
import com.smarthome.monitor.data.model.RecentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseActivityDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeActivities(): Flow<List<RecentActivity>> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.activities())
            .orderByChild("timestamp")
            .limitToLast(20)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activities = mutableListOf<RecentActivity>()
                for (child in snapshot.children) {
                    val activity = child.getValue(RecentActivity::class.java)
                    if (activity != null) {
                        activities.add(activity.copy(id = child.key ?: ""))
                    }
                }
                activities.reverse()
                trySend(activities)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun logActivity(title: String, subtitle: String, type: ActivityType) {
        val ref = db.getReference(Constants.DbPaths.activities()).push()
        val activityId = ref.key ?: throw IllegalStateException("Could not generate activity ID")
        val activity = RecentActivity(
            id = activityId,
            title = title,
            timestamp = System.currentTimeMillis(),
            subtitle = subtitle,
            type = type
        )
        ref.setValue(activity).await()
    }
}
