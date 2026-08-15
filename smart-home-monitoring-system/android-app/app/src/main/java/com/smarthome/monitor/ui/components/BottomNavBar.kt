package com.smarthome.monitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomNavItem(val label: String, val route: String, val filled: ImageVector, val outline: ImageVector) {
    HOME("Home", "home", Icons.Filled.Home, Icons.Outlined.Home),
    FLOOR("Floor", "floor", Icons.Filled.Layers, Icons.Outlined.Layers),
    ALERTS("Alerts", "alerts", Icons.Filled.NotificationsActive, Icons.Outlined.NotificationsActive),
    USAGE("Usage", "usage", Icons.Filled.InsertChart, Icons.Outlined.InsertChart),
    SETTINGS("Settings", "settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun BottomNavBar(
    selected: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    alertBadge: Boolean = false
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .selectableGroup()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = item == selected
                val icon = if (isSelected) item.filled else item.outline
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    onClick = { onItemSelected(item) },
                    color = containerColor,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .weight(1f)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier.padding(top = 0.dp)
                        )
                        if (item == BottomNavItem.ALERTS && alertBadge) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(percent = 50),
                                modifier = Modifier.size(6.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
