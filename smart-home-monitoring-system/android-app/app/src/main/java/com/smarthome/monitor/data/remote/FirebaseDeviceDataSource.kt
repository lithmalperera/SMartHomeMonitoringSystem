package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.data.model.Device
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDeviceDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeDevices(): Flow<List<Device>> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.devices())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                for (child in snapshot.children) {
                    val device = child.getValue(Device::class.java)
                    if (device != null) {
                        devices.add(device.copy(id = child.key ?: ""))
                    }
                }
                trySend(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun turnAllLights(on: Boolean) {
        val devicesRef = db.getReference(Constants.DbPaths.devices())
        val snapshot = devicesRef.get().await()
        val updates = mutableMapOf<String, Any?>()

        for (child in snapshot.children) {
            val type = child.child("type").getValue(String::class.java)
            if (type == "LIGHT") {
                val pathPrefix = "${child.key}/state"
                updates["$pathPrefix/isOn"] = on
                updates["$pathPrefix/lastChangedBy"] = "android"
                if (on) {
                    updates["$pathPrefix/lastOnAt"] = ServerValue.TIMESTAMP
                }
            }
        }

        if (updates.isNotEmpty()) {
            devicesRef.updateChildren(updates).await()
        }
    }

    suspend fun turnAllElectricalDevicesOff() {
        val devicesRef = db.getReference(Constants.DbPaths.devices())
        val snapshot = devicesRef.get().await()
        val updates = mutableMapOf<String, Any?>()

        for (child in snapshot.children) {
            val type = child.child("type").getValue(String::class.java)
            if (type == "OUTLET" || type == "IRON") {
                val pathPrefix = "${child.key}/state"
                updates["$pathPrefix/isOn"] = false
                updates["$pathPrefix/lastChangedBy"] = "android"
            }
        }

        if (updates.isNotEmpty()) {
            devicesRef.updateChildren(updates).await()
        }
    }
}
