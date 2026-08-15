package com.smarthome.monitor.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow

object FloorNavState {
    val currentFloorId = MutableStateFlow<String?>(null)
}
