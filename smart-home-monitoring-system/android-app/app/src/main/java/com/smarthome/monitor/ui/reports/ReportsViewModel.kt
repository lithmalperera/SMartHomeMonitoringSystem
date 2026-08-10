package com.smarthome.monitor.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.core.util.DateUtils
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.UsageRecord
import com.smarthome.monitor.domain.repository.DeviceRepository
import com.smarthome.monitor.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class ReportsPeriod(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class UsageHistoryItem(
    val deviceId: String,
    val name: String,
    val type: DeviceType,
    val periodLabel: String,
    val duration: String,
    val energy: String,
    val sessions: Int,
    val autoCutoffs: Int
)

data class ChartBar(val label: String, val value: Float, val display: String)

data class ReportsSummary(
    val totalKwh: String,
    val totalSessions: Int,
    val totalCutoffs: Int
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: String? = null,
    val selectedPeriod: ReportsPeriod = ReportsPeriod.DAILY,
    val history: List<UsageHistoryItem> = emptyList(),
    val summary: ReportsSummary = ReportsSummary("0 Wh", 0, 0),
    val chart: List<ChartBar> = emptyList()
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val periodFlow = MutableStateFlow(ReportsPeriod.DAILY)
    private val deviceFilterFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReportsUiState> = combine(
        usageRepository.observeUsage(),
        deviceRepository.observeDevices(),
        periodFlow,
        deviceFilterFlow
    ) { usageByDevice, devices, period, selectedDeviceId ->
        val deviceById = devices.associateBy { it.id }
        val summed = summedUsage(usageByDevice, selectedDeviceId, periodDates(period))
            .sortedByDescending { it.second.energyWh }

        ReportsUiState(
            isLoading = false,
            devices = devices,
            selectedDeviceId = selectedDeviceId,
            selectedPeriod = period,
            history = summed.map { (deviceId, record) ->
                UsageHistoryItem(
                    deviceId = deviceId,
                    name = deviceById[deviceId]?.name ?: deviceId,
                    type = deviceById[deviceId]?.type ?: DeviceType.OUTLET,
                    periodLabel = periodLabel(period),
                    duration = DateUtils.formatDuration(record.activeMinutes),
                    energy = DateUtils.formatEnergy(record.energyWh),
                    sessions = record.sessions,
                    autoCutoffs = record.autoCutoffs
                )
            },
            summary = ReportsSummary(
                totalKwh = DateUtils.formatEnergy(summed.sumOf { it.second.energyWh }),
                totalSessions = summed.sumOf { it.second.sessions },
                totalCutoffs = summed.sumOf { it.second.autoCutoffs }
            ),
            chart = buildChart(usageByDevice, deviceById, period, selectedDeviceId)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun selectPeriod(period: ReportsPeriod) {
        periodFlow.value = period
    }

    fun selectDevice(deviceId: String?) {
        deviceFilterFlow.value = deviceId
    }

    private fun periodDates(period: ReportsPeriod): Set<String> = when (period) {
        ReportsPeriod.DAILY -> setOf(DateUtils.todayKey())
        ReportsPeriod.WEEKLY -> DateUtils.lastNDays(7).toSet()
        ReportsPeriod.MONTHLY -> DateUtils.monthWeekGroups().flatMap { it.keys }.toSet()
    }

    private fun periodLabel(period: ReportsPeriod): String = when (period) {
        ReportsPeriod.DAILY -> "Today"
        ReportsPeriod.WEEKLY -> "Last 7 days"
        ReportsPeriod.MONTHLY -> "This month"
    }

    private fun summedUsage(
        usageByDevice: Map<String, Map<String, UsageRecord>>,
        deviceFilter: String?,
        dates: Set<String>
    ): List<Pair<String, UsageRecord>> =
        usageByDevice
            .filterKeys { deviceFilter == null || it == deviceFilter }
            .mapNotNull { (deviceId, byDate) ->
                val records = byDate.filterKeys { it in dates }.values
                if (records.isEmpty()) return@mapNotNull null
                deviceId to UsageRecord(
                    activeMinutes = records.sumOf { it.activeMinutes },
                    sessions = records.sumOf { it.sessions },
                    energyWh = records.sumOf { it.energyWh },
                    autoCutoffs = records.sumOf { it.autoCutoffs }
                )
            }

    private fun buildChart(
        usageByDevice: Map<String, Map<String, UsageRecord>>,
        deviceById: Map<String, Device>,
        period: ReportsPeriod,
        deviceFilter: String?
    ): List<ChartBar> = when (period) {
        ReportsPeriod.DAILY -> {
            val today = DateUtils.todayKey()
            usageByDevice
                .filterKeys { deviceFilter == null || it == deviceFilter }
                .mapNotNull { (deviceId, byDate) ->
                    val record = byDate[today] ?: return@mapNotNull null
                    deviceId to record
                }
                .sortedByDescending { it.second.energyWh }
                .map { (deviceId, record) ->
                    ChartBar(
                        label = deviceById[deviceId]?.name ?: deviceId,
                        value = record.energyWh.toFloat(),
                        display = DateUtils.formatEnergy(record.energyWh)
                    )
                }
        }

        ReportsPeriod.WEEKLY -> DateUtils.lastNDays(7).map { dateKey ->
            var wh = 0.0
            usageByDevice.forEach { (deviceId, byDate) ->
                if (deviceFilter != null && deviceId != deviceFilter) return@forEach
                wh += byDate[dateKey]?.energyWh ?: 0.0
            }
            ChartBar(
                label = DateUtils.shortDayLabel(dateKey),
                value = wh.toFloat(),
                display = DateUtils.formatEnergy(wh)
            )
        }

        ReportsPeriod.MONTHLY -> DateUtils.monthWeekGroups().map { week ->
            var wh = 0.0
            usageByDevice.forEach { (deviceId, byDate) ->
                if (deviceFilter != null && deviceId != deviceFilter) return@forEach
                byDate.forEach { (dateKey, record) ->
                    if (dateKey in week.keys) wh += record.energyWh
                }
            }
            ChartBar(
                label = week.label,
                value = wh.toFloat(),
                display = DateUtils.formatEnergy(wh)
            )
        }
    }
}
