package com.smarthome.monitor.data.model

data class Schedule(
    val id: String = "",
    val deviceId: String = "",
    val onTime: String = "18:00",
    val offTime: String = "06:00",
    val enabled: Boolean = true,
    val lastRunKey: String = ""
)
