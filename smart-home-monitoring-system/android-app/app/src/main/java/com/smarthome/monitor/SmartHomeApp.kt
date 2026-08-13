package com.smarthome.monitor

import android.app.Application
import com.smarthome.monitor.core.automation.HomeAutomation
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartHomeApp : Application() {

    @Inject
    lateinit var automation: HomeAutomation

    override fun onCreate() {
        super.onCreate()
        automation.start()
    }
}
