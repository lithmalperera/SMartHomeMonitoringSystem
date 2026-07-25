package com.smarthome.monitor.data.model

data class Floor(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val gridColumns: Int = 4,
    val gridRows: Int = 4
)
