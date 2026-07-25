package com.smarthome.monitor.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val FLOOR = "floor/{floorId}"
    const val DEVICE = "device/{deviceId}"
    const val SCHEDULE = "schedule/{deviceId}"
    const val REPORTS = "reports"
    const val CAMERA = "camera/{deviceId}"
    const val SETTINGS = "settings"

    fun floor(floorId: String) = "floor/$floorId"
    fun device(deviceId: String) = "device/$deviceId"
    fun schedule(deviceId: String) = "schedule/$deviceId"
    fun camera(deviceId: String) = "camera/$deviceId"
}