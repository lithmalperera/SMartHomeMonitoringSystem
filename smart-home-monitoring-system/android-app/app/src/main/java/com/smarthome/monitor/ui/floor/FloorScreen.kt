package com.smarthome.monitor.ui.floor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.core.util.DeviceStatus
import com.smarthome.monitor.core.util.status
import com.smarthome.monitor.data.model.Device
import com.smarthome.monitor.ui.components.BottomNavBar
import com.smarthome.monitor.ui.components.BottomNavItem
import com.smarthome.monitor.ui.components.DeviceIcon
import com.smarthome.monitor.ui.components.LoadingBox
import com.smarthome.monitor.ui.components.ProfileAvatar

@Composable
fun FloorScreen(
    floorId: String,
    onDeviceClick: (String) -> Unit,
    onAddDevice: () -> Unit,
    onHomeClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onUsageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: FloorViewModel = viewModel()
) {
    LaunchedEffect(floorId) { viewModel.loadFloor(floorId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FloorTopBar(
                floorName = uiState.floorName,
                alertBadge = uiState.alertsCount > 0,
                onFloorSelectorClick = onHomeClick,
                onNotificationsClick = onAlertsClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDevice,
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
                devices = uiState.devices,
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
    alertBadge: Boolean,
    onFloorSelectorClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                onClick = onFloorSelectorClick,
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
                ProfileAvatar(size = 32)
            }
        }
    }
}

@Composable
private fun FloorCanvas(
    devices: List<Device>,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Grid dots
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 2f
                    )
                )
        )
        // Floor plan placeholder
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
                Icon(
                    imageVector = Icons.Default.Architecture,
                    contentDescription = "Floor plan",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                devices.forEach { device ->
                    // deterministic placement for demo
                    val x = ((device.position.x + 1) * 0.12f).coerceIn(0.05f, 0.9f)
                    val y = ((device.position.y + 1) * 0.12f).coerceIn(0.05f, 0.9f)
                    DeviceNode(
                        device = device,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = (x * 100).dp,
                                y = (y * 100).dp
                            )
                            .clickable { onDeviceClick(device.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceNode(device: Device, modifier: Modifier = Modifier) {
    val status = device.status()
    val (borderColor, iconTint) = when (status) {
        DeviceStatus.ON -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
        DeviceStatus.DISCONNECTED -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.outline
        DeviceStatus.OFF -> MaterialTheme.colorScheme.outlineVariant to MaterialTheme.colorScheme.secondary
    }
    val containerColor = when (status) {
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val labelTextColor = when (status) {
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
            shadowElevation = 4.dp,
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
                        .background(MaterialTheme.colorScheme.surface)
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
