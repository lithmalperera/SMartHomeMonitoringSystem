package com.smarthome.monitor.core.util

object Constants {
    const val HOME_ID = "home_001"

    object DbPaths {
        const val HOMES = "homes"
        const val META = "meta"
        const val FLOORS = "floors"
        const val DEVICES = "devices"
        const val STATE = "state"
        const val CONFIG = "config"
        const val SCHEDULES = "schedules"
        const val USAGE = "usage"
        const val USERS = "users"
        const val ACTIVITIES = "activities"
        const val ALERTS = "alerts"

        fun homeRoot() = "$HOMES/$HOME_ID"
        fun floors() = "${homeRoot()}/$FLOORS"
        fun floor(floorId: String) = "${homeRoot()}/$FLOORS/$floorId"
        fun devices() = "${homeRoot()}/$DEVICES"
        fun device(deviceId: String) = "${homeRoot()}/$DEVICES/$deviceId"
        fun deviceState(deviceId: String) = "${device(deviceId)}/$STATE"
        fun deviceConfig(deviceId: String) = "${device(deviceId)}/$CONFIG"
        fun schedules() = "${homeRoot()}/$SCHEDULES"
        fun schedule(scheduleId: String) = "${homeRoot()}/$SCHEDULES/$scheduleId"
        fun usageRoot() = "${homeRoot()}/$USAGE"
        fun usage(deviceId: String) = "${usageRoot()}/$deviceId"
        fun usageDate(deviceId: String, date: String) = "${homeRoot()}/$USAGE/$deviceId/$date"
        fun users() = "$USERS"
        fun user(uid: String) = "$USERS/$uid"
        fun activities() = "${homeRoot()}/$ACTIVITIES"
        fun activity(activityId: String) = "${activities()}/$activityId"
        fun alerts() = "${homeRoot()}/$ALERTS"
        fun alert(alertId: String) = "${alerts()}/$alertId"
    }
}
