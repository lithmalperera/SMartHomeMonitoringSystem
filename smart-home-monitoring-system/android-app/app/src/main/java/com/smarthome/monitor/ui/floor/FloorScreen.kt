package com.smarthome.monitor.ui.floor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.data.model.DeviceType
import com.smarthome.monitor.data.model.GridPosition
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
            TopAppBar(
                title = { Text(uiState.floor.name) },
                navigationIcon = { TextButton(onClick = onBack) { Text("\u2190") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDeviceDialog(true) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Device")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            uiState.devices.isEmpty() -> EmptyState("No devices on this floor", modifier = Modifier.padding(padding))
            else -> PositionedDeviceGrid(
                floor = uiState.floor,
                devices = uiState.devices,
                onDeviceClick = onDeviceClick,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (uiState.showAddDeviceDialog) {
        AddDeviceDialog(
            columns = uiState.floor.gridColumns,
            rows = uiState.floor.gridRows,
            onDismiss = { viewModel.showAddDeviceDialog(false) },
            onAdd = { name, type, room, x, y -> viewModel.addDevice(name, type, room, x, y) }
        )
    }
}

@Composable
private fun PositionedDeviceGrid(
    floor: com.smarthome.monitor.data.model.Floor,
    devices: List<com.smarthome.monitor.data.model.Device>,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        for (y in 0 until floor.gridRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (x in 0 until floor.gridColumns) {
                    val device = devices.firstOrNull { it.position == GridPosition(x, y) }
                    Box(modifier = Modifier.weight(1f)) {
                        if (device != null) {
                            FloorDeviceCell(device = device, onClick = { onDeviceClick(device.id) })
                        } else {
                            Spacer(modifier = Modifier.height(96.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FloorDeviceCell(
    device: com.smarthome.monitor.data.model.Device,
    onClick: () -> Unit
) {
    com.smarthome.monitor.ui.components.DeviceCard(device = device, onClick = onClick)
}

@Composable
private fun AddDeviceDialog(
    columns: Int,
    rows: Int,
    onDismiss: () -> Unit,
    onAdd: (String, DeviceType, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DeviceType.LIGHT) }
    var x by remember { mutableIntStateOf(0) }
    var y by remember { mutableIntStateOf(0) }
    val typeOptions = DeviceType.values()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, singleLine = true)

                Text("Type")
                DropdownTypeSelector(selected = type, options = typeOptions, onSelect = { type = it })

                Text("Grid cell")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IntStepper(label = "X", value = x, range = 0 until columns, onChange = { x = it })
                    IntStepper(label = "Y", value = y, range = 0 until rows, onChange = { y = it })
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, type, room, x, y) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownTypeSelector(selected: DeviceType, options: Array<DeviceType>, onSelect: (DeviceType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt.name) }, onClick = { onSelect(opt); expanded = false })
            }
        }
    }
}

@Composable
private fun IntStepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (value > range.first) onChange(value - 1) }) { Text("-") }
        Text("$label: $value", modifier = Modifier.padding(horizontal = 4.dp))
        TextButton(onClick = { if (value < range.last) onChange(value + 1) }) { Text("+") }
    }
}