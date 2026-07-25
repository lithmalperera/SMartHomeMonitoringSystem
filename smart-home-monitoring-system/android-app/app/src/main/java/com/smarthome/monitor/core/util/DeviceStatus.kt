package com.smarthome.monitor.core.util

import com.smarthome.monitor.data.model.Device

enum class DeviceStatus { DISCONNECTED, ERROR, ON, OFF }

fun Device.status(): DeviceStatus = when {
    !state.online -> DeviceStatus.DISCONNECTED
    state.error -> DeviceStatus.ERROR
    state.isOn -> DeviceStatus.ON
    else -> DeviceStatus.OFF
}
