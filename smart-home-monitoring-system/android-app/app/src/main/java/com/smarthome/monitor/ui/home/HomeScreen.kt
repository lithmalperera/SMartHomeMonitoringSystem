package com.smarthome.monitor.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.ui.components.EmptyState
import com.smarthome.monitor.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFloorClick: (String) -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Home") },
                actions = {
                    TextButton(onClick = onReportsClick) { Text("Reports") }
                    TextButton(onClick = onSettingsClick) { Text("Settings") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddFloorDialog(true) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Floor")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            uiState.floors.isEmpty() -> EmptyState("No floors yet", modifier = Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.alerts.isNotEmpty()) {
                    item {
                        Text("Safety Alerts", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                    }
                    items(uiState.alerts) { alert ->
                        AssistChip(
                            onClick = { onFloorClick("floor_ground") },
                            label = { Text("⚠ ${alert.deviceName} ON for ${alert.onMinutes} min") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                item {
                    Text("Floors", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(uiState.floors) { summary ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFloorClick(summary.floor.id) },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(summary.floor.name, fontSize = 18.sp)
                                Text(
                                    "${summary.deviceCount} devices · ${summary.onCount} ON",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("\u203A", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddFloorDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.showAddFloorDialog(false) },
            title = { Text("Add Floor") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Floor name") },
                    singleLine = true
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.addFloor(name) }) { Text("Add") } },
            dismissButton = { TextButton(onClick = { viewModel.showAddFloorDialog(false) }) { Text("Cancel") } }
        )
    }
}