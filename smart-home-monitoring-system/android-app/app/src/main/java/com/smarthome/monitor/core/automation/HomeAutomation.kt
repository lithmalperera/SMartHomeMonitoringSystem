package com.smarthome.monitor.core.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smarthome.monitor.core.util.DateUtils
import com.smarthome.monitor.data.model.AlertSeverity
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.domain.repository.AlertRepository
import com.smarthome.monitor.domain.repository.DeviceRepository
import com.smarthome.monitor.domain.repository.ScheduleRepository
import com.smarthome.monitor.domain.repository.UsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeAutomation @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val scheduleRepository: ScheduleRepository,
    private val usageRepository: UsageRepository,
    private val alertRepository: AlertRepository,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val prevIsOn = ConcurrentHashMap<String, Boolean>()
    private val watchedCutoffAt = ConcurrentHashMap<String, Long>()

    private val devicesFlow = deviceRepository.observeDevices()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val schedulesFlow = scheduleRepository.observeSchedules()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun start() {
        createNotificationChannel()
        scope.launch { runUsageWatcher() }
        scope.launch { runSafetyWatchdog() }
        scope.launch { runScheduleRunner() }
    }

    private suspend fun runUsageWatcher() {
        devicesFlow.collect { devices ->
            for (device in devices) {
                val wasOn = prevIsOn[device.id]
                if (wasOn == true && !device.state.isOn) {
                    val lastOnAt = device.state.lastOnAt
                    if (device.type == DeviceType.IRON && lastOnAt > 0 &&
                        System.currentTimeMillis() - lastOnAt >= device.config.maxActiveMinutes * 60000L
                    ) {
                        handleCutoff(device)
                    }
                    if (lastOnAt > 0) {
                        val minutes = (System.currentTimeMillis() - lastOnAt) / 60000
                        if (minutes >= 1) {
                            val energyWh = (minutes / 60.0) * device.config.wattage
                            runCatching {
                                usageRepository.recordUsage(device.id, minutes, energyWh)
                            }
                        }
                    }
                }
                prevIsOn[device.id] = device.state.isOn
            }
        }
    }

    private suspend fun handleCutoff(device: Device) {
        if (watchedCutoffAt.putIfAbsent(device.id, device.state.lastOnAt) != null) return
        runCatching {
            usageRepository.recordCutoff(device.id)
            alertRepository.addAlert(
                deviceId = device.id,
                title = "Safety cutoff",
                message = "${device.name} was automatically turned OFF after exceeding the ${device.config.maxActiveMinutes} min safety limit.",
                severity = AlertSeverity.CRITICAL
            )
            showCutoffNotification(device.name, device.config.maxActiveMinutes)
        }
    }

    private suspend fun runSafetyWatchdog() {
        while (scope.isActive) {
            val devices = devicesFlow.value
            for (device in devices) {
                if (device.type != DeviceType.IRON || !device.state.isOn) continue
                val limitMin = device.config.maxActiveMinutes
                val lastOnAt = device.state.lastOnAt
                if (limitMin <= 0 || lastOnAt <= 0) continue
                if (System.currentTimeMillis() - lastOnAt < limitMin * 60000L) continue
                if (watchedCutoffAt.putIfAbsent(device.id, lastOnAt) != null) continue

                runCatching {
                    deviceRepository.setPower(device.id, false)
                    usageRepository.recordCutoff(device.id)
                    alertRepository.addAlert(
                        deviceId = device.id,
                        title = "Safety cutoff",
                        message = "${device.name} was automatically turned OFF after exceeding the $limitMin min safety limit.",
                        severity = AlertSeverity.CRITICAL
                    )
                    showCutoffNotification(device.name, limitMin)
                }
            }
            delay(1000)
        }
    }

    private suspend fun runScheduleRunner() {
        while (scope.isActive) {
            val now = Calendar.getInstance()
            val nowKey = String.format(
                "%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE)
            )
            val today = DateUtils.todayKey()

            for (schedule in schedulesFlow.value) {
                if (!schedule.enabled) continue
                if (schedule.onTime == nowKey && schedule.lastRunKey != "$today-ON") {
                    runCatching {
                        deviceRepository.setPower(schedule.deviceId, true)
                        scheduleRepository.updateLastRunKey(schedule.id, "$today-ON")
                    }
                }
                if (schedule.offTime == nowKey && schedule.lastRunKey != "$today-OFF") {
                    runCatching {
                        deviceRepository.setPower(schedule.deviceId, false)
                        scheduleRepository.updateLastRunKey(schedule.id, "$today-OFF")
                    }
                }
            }
            delay(60_000)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Safety alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun showCutoffNotification(deviceName: String, limitMin: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Safety cutoff")
            .setContentText("$deviceName was automatically turned OFF after $limitMin min")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(deviceName.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "safety_alerts"
    }
}
