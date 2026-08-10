package com.smarthome.monitor.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val FLOOR = "floor/{floorId}"
    const val FLOOR_SETUP = "floor_setup"
    const val DEVICE = "device/{deviceId}"
    const val DEVICE_PLACEMENT = "device_placement/{floorId}"
    const val SCHEDULE = "schedule/{deviceId}"
    const val ALERTS = "alerts"
    const val USAGE = "usage"
    const val SETTINGS = "settings"

    fun floor(floorId: String) = "floor/$floorId"
    fun device(deviceId: String) = "device/$deviceId"
    fun schedule(deviceId: String) = "schedule/$deviceId"
    fun devicePlacement(floorId: String) = "device_placement/$floorId"
}
