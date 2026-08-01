package com.smarthome.monitor.ui.reports

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.UsageRecord
import java.text.SimpleDateFormat
import java.util.*

data class DeviceUsageRow(
    val device: Device,
    val record: UsageRecord
)

data class Totals(
    val activeMinutes: Long,
    val sessions: Int,
    val energyWh: Double
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val date: String = "",
    val rows: List<DeviceUsageRow> = emptyList(),
    val totals: Totals = Totals(0, 0, 0.0)
)

class ReportsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        load(fmt.format(Date()))
    }

    fun setDate(date: String) {
        if (date.isNotBlank()) load(date)
    }

    fun shiftDate(days: Int) {
        val current = runCatching { fmt.parse(uiState.value.date) }.getOrNull() ?: Date()
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.DAY_OF_MONTH, days)
        }
        load(fmt.format(cal.time))
    }

    private fun load(date: String) {
        val rows = listOf(
            DeviceUsageRow(
                Device(id = "dev_light_living", name = "Living Room Light", type = DeviceType.LIGHT),
                UsageRecord(date = date, activeMinutes = 120, sessions = 6, energyWh = 40.0)
            ),
            DeviceUsageRow(
                Device(id = "dev_outlet_tv", name = "TV Outlet", type = DeviceType.OUTLET),
                UsageRecord(date = date, activeMinutes = 90, sessions = 3, energyWh = 165.0)
            ),
            DeviceUsageRow(
                Device(id = "dev_iron_living", name = "Iron", type = DeviceType.IRON),
                UsageRecord(date = date, activeMinutes = 20, sessions = 1, energyWh = 366.6)
            )
        )
        _uiState.value = ReportsUiState(
            isLoading = false,
            date = date,
            rows = rows,
            totals = Totals(
                activeMinutes = rows.sumOf { it.record.activeMinutes },
                sessions = rows.sumOf { it.record.sessions },
                energyWh = rows.sumOf { it.record.energyWh }
            )
        )
    }
}