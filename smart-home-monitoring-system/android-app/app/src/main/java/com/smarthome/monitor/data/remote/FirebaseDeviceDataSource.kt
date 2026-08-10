package com.smarthome.monitor.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceConfig
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition
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
                        val isOn = child.child("state/isOn").getValue(Boolean::class.java) ?: device.state.isOn
                        devices.add(
                            device.copy(
                                id = child.key ?: "",
                                state = device.state.copy(isOn = isOn)
                            )
                        )
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

    suspend fun addDevice(
        name: String,
        type: DeviceType,
        floorId: String,
        room: String,
        position: GridPosition,
        maxActiveMinutes: Int,
        gangs: Int
    ) {
        val deviceId = "dev_" + randomDeviceId()
        val ref = db.getReference(Constants.DbPaths.device(deviceId))

        val state = mutableMapOf<String, Any>(
            "isOn" to false,
            "online" to true,
            "error" to false,
            "lastOnAt" to 0L,
            "lastChangedBy" to "android"
        )

        val wattage = when (type) {
            DeviceType.IRON -> 1000
            DeviceType.LIGHT -> 12
            DeviceType.OUTLET -> 120
            DeviceType.CAMERA -> 5
            DeviceType.LOCK -> 2
            DeviceType.THERMOSTAT -> 1500
            DeviceType.SWITCH_PANEL -> 0
        }

        val config = mapOf(
            "wattage" to wattage,
            "maxActiveMinutes" to if (type == DeviceType.IRON) maxActiveMinutes else 0
        )

        when (type) {
            DeviceType.SWITCH_PANEL -> {
                val switches = mutableMapOf<String, Boolean>()
                for (i in 1..gangs) switches["s$i"] = false
                state["switches"] = switches
            }
            DeviceType.CAMERA -> {
                state["snapshotUrl"] = "https://picsum.photos/seed/$deviceId/640/360"
                state["streamUrl"] = "rtsp://192.168.1.100/stream"
            }
            DeviceType.THERMOSTAT -> state["targetTemperature"] = 24
            else -> Unit
        }

        val devicePayload = mapOf(
            "name" to name,
            "type" to type.name,
            "floorId" to floorId,
            "room" to room,
            "position" to mapOf("x" to position.x, "y" to position.y),
            "state" to state,
            "config" to config
        )

        ref.setValue(devicePayload).await()

        if (type == DeviceType.LIGHT) {
            db.getReference(Constants.DbPaths.schedule("sch_$deviceId")).setValue(
                mapOf(
                    "deviceId" to deviceId,
                    "onTime" to "18:00",
                    "offTime" to "06:00",
                    "enabled" to false,
                    "lastRunKey" to ""
                )
            ).await()
        }
    }

    suspend fun deleteDevice(deviceId: String) {
        db.getReference(Constants.DbPaths.device(deviceId)).removeValue().await()
        db.getReference(Constants.DbPaths.schedule("sch_$deviceId")).removeValue().await()
    }

    fun observeDevice(deviceId: String): Flow<Device?> = callbackFlow {
        val ref = db.getReference(Constants.DbPaths.device(deviceId))
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                val device = snapshot.getValue(Device::class.java)?.copy(id = snapshot.key ?: "")
                val fixed = device?.let {
                    val isOn = snapshot.child("state/isOn").getValue(Boolean::class.java) ?: it.state.isOn
                    it.copy(state = it.state.copy(isOn = isOn))
                }
                trySend(fixed)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setPower(deviceId: String, on: Boolean) {
        val updates = mutableMapOf<String, Any>(
            "isOn" to on,
            "lastChangedBy" to "android"
        )
        if (on) {
            updates["lastOnAt"] = ServerValue.TIMESTAMP
        }
        db.getReference(Constants.DbPaths.deviceState(deviceId)).updateChildren(updates).await()
    }

    suspend fun setPanelSwitch(deviceId: String, key: String, on: Boolean) {
        val stateRef = db.getReference(Constants.DbPaths.deviceState(deviceId))
        val snapshot = stateRef.get().await()

        val currentSwitches = mutableMapOf<String, Boolean>()
        snapshot.child("switches").children.forEach { child ->
            currentSwitches[child.key ?: ""] = child.getValue(Boolean::class.java) ?: false
        }
        currentSwitches[key] = on

        stateRef.updateChildren(
            mapOf(
                "switches" to currentSwitches,
                "isOn" to currentSwitches.values.any { it },
                "lastChangedBy" to "android"
            )
        ).await()
    }

    suspend fun updateConfig(deviceId: String, config: DeviceConfig) {
        db.getReference(Constants.DbPaths.deviceConfig(deviceId)).setValue(config).await()
    }

    suspend fun refreshSnapshot(deviceId: String) {
        val stateRef = db.getReference(Constants.DbPaths.deviceState(deviceId))
        val snapshot = stateRef.get().await()
        val currentUrl = snapshot.child("snapshotUrl").getValue(String::class.java)
        if (currentUrl.isNullOrBlank()) return

        val base = currentUrl.substringBefore("?")
        stateRef.updateChildren(
            mapOf(
                "snapshotUrl" to "$base?t=${System.currentTimeMillis()}",
                "lastChangedBy" to "android"
            )
        ).await()
    }

    private fun randomDeviceId(length: Int = 8): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
}
