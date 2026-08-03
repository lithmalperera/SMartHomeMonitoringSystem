package com.smarthome.monitor.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.DoorSliding
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.R
import com.smarthome.monitor.data.model.Floor
import com.smarthome.monitor.ui.components.BottomNavBar
import com.smarthome.monitor.ui.components.BottomNavItem
import com.smarthome.monitor.ui.components.LoadingBox
import com.smarthome.monitor.ui.components.ProfileAvatar

@Composable
fun HomeScreen(
    onFloorSelected: (String) -> Unit,
    onAddFloor: () -> Unit,
    onAddDevice: () -> Unit,
    onAlertsClick: () -> Unit,
    onUsageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedNav = BottomNavItem.HOME

    Scaffold(
        topBar = {
            HomeTopBar(
                alertBadge = uiState.alertsCount > 0,
                onNotificationsClick = onAlertsClick
            )
        },
        bottomBar = {
            BottomNavBar(
                selected = selectedNav,
                alertBadge = uiState.alertsCount > 0,
                onItemSelected = { item ->
                    when (item) {
                        BottomNavItem.HOME -> {}
                        BottomNavItem.FLOOR -> {
                            val firstFloor = uiState.floors.firstOrNull()
                            if (firstFloor != null) onFloorSelected(firstFloor.id)
                        }
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
            else -> HomeContent(
                uiState = uiState,
                onFloorSelected = onFloorSelected,
                onAddFloor = onAddFloor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    alertBadge: Boolean,
    onNotificationsClick: () -> Unit
) {
    Surface(
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(size = 40)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Lumina Home",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF0047AB),
                    fontWeight = FontWeight.Bold
                )
            }
            Box {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1A1A1A)
                    )
                }
                if (alertBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onFloorSelected: (String) -> Unit,
    onAddFloor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderSection(uiState)
        
        HomeStatusSection(uiState.lastSync)
        
        StatsSection(uiState)
        
        FloorsSection(
            floors = uiState.floors,
            onFloorSelected = onFloorSelected,
            onAddFloor = onAddFloor
        )
        
        QuickActionsSection()
        
        RecentActivitySection(activities = uiState.recentActivities)
    }
}

@Composable
private fun HeaderSection(uiState: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(
                text = "Welcome, ${uiState.userName}! 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "Welcome back to your smart home",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, Modifier.size(16.dp), tint = Color(0xFF0047AB))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = uiState.date, fontSize = 14.sp, color = Color(0xFF0047AB), fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, Modifier.size(16.dp), tint = Color(0xFF0047AB))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = uiState.time, fontSize = 14.sp, color = Color(0xFF0047AB), fontWeight = FontWeight.Medium)
            }
        }
        
        Image(
            painter = painterResource(id = R.drawable.img_home_header),
            contentDescription = "Home Header",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun HomeStatusSection(lastSync: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Home Status", fontSize = 12.sp, color = Color.Gray)
                    Text("Online", fontSize = 16.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
            
            VerticalDivider(modifier = Modifier.height(32.dp).width(1.dp), color = Color(0xFFF0F0F0))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last Sync", fontSize = 12.sp, color = Color.Gray)
                    Text(lastSync, fontSize = 16.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
private fun StatsSection(uiState: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Architecture, count = uiState.floorsCount.toString(), label = "Floors", iconColor = Color(0xFF2196F3), bgColor = Color(0xFFE3F2FD))
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Lightbulb, count = uiState.totalDevices.toString(), label = "Devices", iconColor = Color(0xFF9C27B0), bgColor = Color(0xFFF3E5F5))
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.SignalCellularAlt, count = uiState.onlineCount.toString(), label = "Online", iconColor = Color(0xFF4CAF50), bgColor = Color(0xFFE8F5E9))
        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Notifications, count = uiState.alertsCount.toString(), label = "Alerts", iconColor = Color(0xFFF44336), bgColor = Color(0xFFFFEBEE))
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: ImageVector, count: String, label: String, iconColor: Color, bgColor: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(6.dp).size(20.dp), tint = iconColor)
            }
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun FloorsSection(
    floors: List<Floor>,
    onFloorSelected: (String) -> Unit,
    onAddFloor: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Floors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAddFloor() }
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, Modifier.size(16.dp), tint = Color(0xFF0047AB))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Floor", color = Color(0xFF0047AB), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            floors.forEach { floor ->
                FloorListItem(floor = floor, onClick = { onFloorSelected(floor.id) })
            }
        }
    }
}

@Composable
private fun FloorListItem(floor: Floor, onClick: () -> Unit) {
    val imageRes = when (floor.name) {
        "Ground Floor" -> R.drawable.ground_floor
        "First Floor" -> R.drawable.first_floor
        "Garage" -> R.drawable.garage
        else -> null
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = floor.name,
                    modifier = Modifier.size(80.dp, 60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(80.dp, 60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Architecture, contentDescription = null, tint = Color.LightGray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = floor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (floor.name == "Garage") Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (floor.name == "Garage") "• Offline" else "• Online",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = if (floor.name == "Garage") Color(0xFFFF9800) else Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    val deviceCount = when (floor.name) {
                        "Ground Floor" -> 12
                        "First Floor" -> 8
                        else -> 4
                    }
                    Text(
                        text = "$deviceCount Devices",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
private fun QuickActionsSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(icon = Icons.Outlined.Lightbulb, label = "All Lights On", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
            QuickActionButton(icon = Icons.Outlined.Lightbulb, label = "All Lights Off", color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
            QuickActionButton(icon = Icons.Outlined.Power, label = "All Electrical\nDevices Off", color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
            QuickActionButton(icon = Icons.Outlined.NotificationsOff, label = "All Alerts\nMuted", color = Color(0xFF9C27B0), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentActivitySection(activities: List<RecentActivity>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                activities.forEachIndexed { index, activity ->
                    ActivityItem(activity = activity)
                    if (index < activities.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF5F5F5)))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    onClick = { /* View All */ },
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "View All Activity",
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF0047AB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: RecentActivity) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (icon, bgColor, iconColor) = when (activity.type) {
            ActivityType.DOOR -> Triple(Icons.Outlined.DoorSliding, Color(0xFFE0F7FA), Color(0xFF00BCD4))
            ActivityType.CLIMATE -> Triple(Icons.Default.Thermostat, Color(0xFFF3E5F5), Color(0xFF9C27B0))
            ActivityType.LOCK -> Triple(Icons.Default.Lock, Color(0xFFFFF9C4), Color(0xFFFBC02D))
            ActivityType.MOTION -> Triple(Icons.Default.Sensors, Color(0xFFFFEBEE), Color(0xFFF44336))
        }
        
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${activity.time} • ${activity.subtitle}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
