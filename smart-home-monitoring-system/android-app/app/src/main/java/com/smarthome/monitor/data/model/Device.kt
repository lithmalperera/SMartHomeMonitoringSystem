package com.smarthome.monitor.data.model

enum class DeviceType { OUTLET, SWITCH_PANEL, IRON, LIGHT, CAMERA, LOCK, THERMOSTAT }

data class Device(
    val id: String = "",
    val name: String = "",
    val type: DeviceType = DeviceType.OUTLET,
    val floorId: String = "",
    val room: String = "",
    val position: GridPosition = GridPosition(),
    val state: DeviceState = DeviceState(),
    val config: DeviceConfig = DeviceConfig()
)

data class GridPosition(val x: Int = 0, val y: Int = 0)

data class DeviceState(
    val isOn: Boolean = false,
    val switches: Map<String, Boolean> = emptyMap(),
    val online: Boolean = true,
    val error: Boolean = false,
    val streamUrl: String = "",
    val snapshotUrl: String = "",
    val targetTemperature: Int = 24,
    val lastOnAt: Long = 0L,
    val lastChangedBy: String = ""
)

data class DeviceConfig(
    val maxActiveMinutes: Int = 30,
    val wattage: Int = 0
)
