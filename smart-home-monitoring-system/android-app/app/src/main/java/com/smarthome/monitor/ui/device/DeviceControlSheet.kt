package com.smarthome.monitor.ui.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(uiState.device?.name ?: "Device") },
        text = { Text("Controls coming soon") },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}