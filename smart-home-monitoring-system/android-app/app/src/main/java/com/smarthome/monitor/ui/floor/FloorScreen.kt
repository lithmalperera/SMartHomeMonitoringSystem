package com.smarthome.monitor.ui.floor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarthome.monitor.R
import com.smarthome.monitor.core.util.DeviceStatus
import com.smarthome.monitor.core.util.status
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.ui.components.BottomNavBar
import com.smarthome.monitor.ui.components.BottomNavItem
import com.smarthome.monitor.ui.components.DeviceIcon
import com.smarthome.monitor.ui.components.LoadingBox

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun FloorScreen(
    floorId: String,
    onDeviceClick: (String) -> Unit,
    onAddDevice: (String) -> Unit,
    onSelectFloor: (String) -> Unit,
    onHomeClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onUsageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: FloorViewModel = hiltViewModel()
) {
    LaunchedEffect(floorId) { viewModel.loadFloor(floorId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FloorTopBar(
                floorName = uiState.floorName,
                floors = uiState.floors,
                currentFloorId = floorId,
                alertBadge = uiState.alertsCount > 0,
                onSelectFloor = onSelectFloor,
                onNotificationsClick = onAlertsClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddDevice(floorId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Device")
            }
        },
        bottomBar = {
            BottomNavBar(
                selected = BottomNavItem.FLOOR,
                alertBadge = uiState.alertsCount > 0,
                onItemSelected = { item ->
                    when (item) {
                        BottomNavItem.HOME -> onHomeClick()
                        BottomNavItem.FLOOR -> {}
                        BottomNavItem.ALERTS -> onAlertsClick()
                        BottomNavItem.USAGE -> onUsageClick()
                        BottomNavItem.SETTINGS -> onSettingsClick()
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            else -> FloorCanvas(
                imageUrl = uiState.imageUrl,
                devices = uiState.devices,
                gridColumns = uiState.gridColumns,
                gridRows = uiState.gridRows,
                onDeviceClick = onDeviceClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun FloorTopBar(
    floorName: String,
    floors: List<Floor>,
    currentFloorId: String,
    alertBadge: Boolean,
    onSelectFloor: (String) -> Unit,
    onNotificationsClick: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                Surface(
                    onClick = { menuOpen = true },
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = floorName,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Switch floor",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    floors.forEach { floor ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = floor.name,
                                    fontWeight = if (floor.id == currentFloorId) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                menuOpen = false
                                if (floor.id != currentFloorId) onSelectFloor(floor.id)
                            }
                        )
                    }
                    if (floors.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No floors yet") },
                            onClick = { menuOpen = false }
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Box {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (alertBadge) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun FloorCanvas(
    imageUrl: String?,
    devices: List<Device>,
    gridColumns: Int,
    gridRows: Int,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // 1. Floor plan image (BOTTOM LAYER)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Floor plan",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = "Floor plan",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        // 2. Grid dots & Blue Dimmer (MIDDLE LAYER)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0047AB).copy(alpha = 0.1f), // Blue dimmer tint
                            Color.Transparent
                        ),
                        radius = 3000f // Large radius for a soft dimmer effect
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 3f
                    )
                )
        )

        // 3. Devices (TOP LAYER)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val cellW = maxWidth / gridColumns.coerceAtLeast(1)
            val cellH = maxHeight / gridRows.coerceAtLeast(1)
            devices.forEach { device ->
                DeviceNode(
                    device = device,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (cellW * device.position.x).coerceAtLeast(0.dp),
                            y = (cellH * device.position.y).coerceAtLeast(0.dp)
                        )
                        .clickable { onDeviceClick(device.id) }
                )
            }
        }
    }
}

@Composable
private fun DeviceNode(device: Device, modifier: Modifier = Modifier) {
    val status = device.status()
    val (borderColor, iconTint) = when (status) {
        DeviceStatus.ON -> Color(0xFFFFC107) to Color(0xFFFFC107)
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
        DeviceStatus.DISCONNECTED -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.outline
        DeviceStatus.OFF -> MaterialTheme.colorScheme.outlineVariant to MaterialTheme.colorScheme.secondary
    }
    val containerColor = when (status) {
        DeviceStatus.ON -> Color(0xFFFFF9C4)
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val labelTextColor = when (status) {
        DeviceStatus.ON -> Color(0xFF795548)
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            shadowElevation = 6.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(containerColor)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (status == DeviceStatus.ON) Color(0xFFFFFDE7) else MaterialTheme.colorScheme.surface)
                ) {
                    DeviceIcon(
                        type = device.type,
                        modifier = Modifier.align(Alignment.Center),
                        tint = iconTint
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = containerColor,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = device.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = labelTextColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
