package com.smarthome.monitor.ui.device

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.ui.components.StatusBadge
import com.smarthome.monitor.core.util.status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlSheet(
    deviceId: String,
    onScheduleClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: DeviceControlViewModel = viewModel()
) {
    LaunchedEffect(deviceId) { viewModel.loadDevice(deviceId) }
    val uiState by viewModel.uiState.collectAsState()
    val device = uiState.device
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(enabled = true, onBack = onDismiss)

    if (device == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(device.name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(device.room, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = device.status())
            }
            Spacer(Modifier.height(16.dp))

            when (device.type) {
                DeviceType.OUTLET, DeviceType.LIGHT -> ToggleContent(device, viewModel)
                DeviceType.SWITCH_PANEL -> SwitchPanelContent(device, viewModel)
                DeviceType.IRON -> IronContent(device, uiState.elapsedMinutes, viewModel)
                DeviceType.CAMERA -> CameraShortcutContent(onCameraClick)
                DeviceType.LOCK, DeviceType.THERMOSTAT -> ToggleContent(device, viewModel)
            }

            if (device.type in setOf(DeviceType.LIGHT, DeviceType.OUTLET)) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onScheduleClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Schedule")
                }
            }
        }
    }
}

@Composable
private fun ToggleContent(device: Device, viewModel: DeviceControlViewModel) {
    val disabled = !device.state.online
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (device.state.isOn) "ON" else "OFF")
        Switch(
            checked = device.state.isOn,
            enabled = !disabled,
            onCheckedChange = { viewModel.setPower(it) }
        )
    }
}

@Composable
private fun SwitchPanelContent(device: Device, viewModel: DeviceControlViewModel) {
    if (device.state.switches.isEmpty()) {
        Text("No switches configured")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        device.state.switches.entries.toList().forEachIndexed { index, entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gang ${index + 1} (${entry.key})")
                Switch(
                    checked = entry.value,
                    enabled = device.state.online,
                    onCheckedChange = { viewModel.setPanelSwitch(entry.key, it) }
                )
            }
        }
    }
}

@Composable
private fun IronContent(device: Device, elapsedMinutes: Long, viewModel: DeviceControlViewModel) {
    val limit = device.config.maxActiveMinutes
    var limitText by remember(limit) { mutableStateOf(limit.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (device.state.isOn) "ON for ${elapsedMinutes} min" else "OFF")
        Switch(checked = device.state.isOn, onCheckedChange = { viewModel.setPower(it) })
    }
    Spacer(Modifier.height(12.dp))
    Text("Limit: $limit min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = limitText,
            onValueChange = { limitText = it.filter { c -> c.isDigit() } },
            label = { Text("maxActiveMinutes") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            enabled = device.state.online
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = {
            limitText.toIntOrNull()?.let { viewModel.setMaxActiveMinutes(it) }
        }) { Text("Set") }
    }
}

@Composable
private fun CameraShortcutContent(onCameraClick: () -> Unit) {
    Button(onClick = onCameraClick, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.Videocam, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Open camera")
    }
}