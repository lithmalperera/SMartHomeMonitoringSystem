package com.smarthome.monitor.ui.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smarthome.monitor.core.util.DeviceStatus
import com.smarthome.monitor.ui.components.StatusBadge
import com.smarthome.monitor.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(deviceId: String, onBack: () -> Unit, viewModel: CameraViewModel = viewModel()) {
    LaunchedEffect(deviceId) { viewModel.load(deviceId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera") },
                navigationIcon = { TextButton(onClick = onBack) { Text("\u2190") } }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(uiState.device.name, fontSize = 18.sp)
                    Text("Device: ${uiState.device.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = if (uiState.online) DeviceStatus.ON else DeviceStatus.DISCONNECTED)
            }

            Surface(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                val url = uiState.device.state.snapshotUrl.let { base ->
                    if (uiState.refreshKey == 0) base else "$base?k=${uiState.refreshKey}"
                }
                AsyncImage(
                    model = url,
                    contentDescription = "Snapshot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text("Stream URL", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(uiState.device.state.streamUrl, fontSize = 12.sp)

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val next = uiState.refreshKey + 1
                    viewModel.refreshSnapshot(next)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh snapshot")
            }
        }
    }
}