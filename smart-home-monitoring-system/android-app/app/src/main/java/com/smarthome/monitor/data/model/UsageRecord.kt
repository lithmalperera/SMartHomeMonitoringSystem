package com.smarthome.monitor.data.model

data class UsageRecord(
    val date: String = "",
    val activeMinutes: Long = 0,
    val sessions: Int = 0,
    val energyWh: Double = 0.0
)
