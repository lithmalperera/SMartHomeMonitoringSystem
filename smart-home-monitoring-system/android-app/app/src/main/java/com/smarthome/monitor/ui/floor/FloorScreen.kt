package com.smarthome.monitor.ui.floor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.ui.components.DeviceCard
import com.smarthome.monitor.ui.components.EmptyState
import com.smarthome.monitor.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorScreen(
    floorId: String,
    onDeviceClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FloorViewModel = viewModel()
) {
    LaunchedEffect(floorId) { viewModel.loadFloor(floorId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(uiState.floorName) }, navigationIcon = {
                TextButton(onClick = onBack) { Text("\u2190") }
            })
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            uiState.devices.isEmpty() -> EmptyState("No devices on this floor", modifier = Modifier.padding(padding))
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.devices) { device ->
                    DeviceCard(device = device, onClick = { onDeviceClick(device.id) })
                }
            }
        }
    }
}