package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.data.model.Alert
import com.smarthome.monitor.data.model.AlertSeverity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAlertDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeAlerts(): Flow<List<Alert>> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.alerts())
            .orderByChild("timestamp")
            .limitToLast(50)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = mutableListOf<Alert>()
                for (child in snapshot.children) {
                    val alert = child.getValue(Alert::class.java)
                    if (alert != null) {
                        alerts.add(alert.copy(id = child.key ?: ""))
                    }
                }
                alerts.reverse()
                trySend(alerts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun markRead(alertId: String) {
        db.getReference(Constants.DbPaths.alert(alertId))
            .child("read")
            .setValue(true)
            .await()
    }

    suspend fun markAllRead() {
        val ref = db.getReference(Constants.DbPaths.alerts())
        val snapshot = ref.get().await()
        val updates = mutableMapOf<String, Any?>()
        for (child in snapshot.children) {
            updates["${child.key}/read"] = true
        }
        if (updates.isNotEmpty()) {
            ref.updateChildren(updates).await()
        }
    }

    suspend fun addAlert(
        deviceId: String,
        title: String,
        message: String,
        severity: AlertSeverity
    ) {
        val ref = db.getReference(Constants.DbPaths.alerts()).push()
        val alertId = ref.key ?: throw IllegalStateException("Could not generate alert ID")
        val alert = Alert(
            id = alertId,
            deviceId = deviceId,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            severity = severity,
            read = false
        )
        ref.setValue(alert).await()
    }
}
