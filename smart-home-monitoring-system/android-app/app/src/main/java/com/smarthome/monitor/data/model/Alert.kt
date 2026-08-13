package com.smarthome.monitor.data.model

enum class AlertSeverity { CRITICAL, SECURITY, INFO, ROUTINE }

data class Alert(
    val id: String = "",
    val deviceId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val severity: AlertSeverity = AlertSeverity.INFO,
    val read: Boolean = false
)
