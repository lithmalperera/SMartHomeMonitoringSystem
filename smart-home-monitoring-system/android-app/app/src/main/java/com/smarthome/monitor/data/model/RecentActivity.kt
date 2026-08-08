package com.smarthome.monitor.data.model

enum class ActivityType { DOOR, CLIMATE, LOCK, MOTION }

data class RecentActivity(
    val id: String = "",
    val title: String = "",
    val timestamp: Long = 0L,
    val subtitle: String = "",
    val type: ActivityType = ActivityType.DOOR
)
