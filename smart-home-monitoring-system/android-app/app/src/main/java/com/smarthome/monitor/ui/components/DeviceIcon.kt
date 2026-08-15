package com.smarthome.monitor.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.smarthome.monitor.data.model.DeviceType

@Composable
fun DeviceIcon(
    type: DeviceType,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    when (val vector = deviceIconVector(type)) {
        null -> Text(
            text = deviceIconEmoji(type),
            modifier = modifier,
            color = tint
        )
        else -> Icon(
            imageVector = vector,
            contentDescription = type.name,
            modifier = modifier,
            tint = tint
        )
    }
}

fun deviceIconVector(type: DeviceType): ImageVector? = when (type) {
    DeviceType.LIGHT -> Icons.Default.Lightbulb
    DeviceType.OUTLET -> Icons.Default.Power
    DeviceType.SWITCH_PANEL -> Icons.Default.ToggleOn
    DeviceType.CAMERA -> Icons.Default.Videocam
    DeviceType.LOCK -> Icons.Default.Lock
    DeviceType.THERMOSTAT -> Icons.Default.Thermostat
    DeviceType.IRON -> Icons.Default.Iron
}

fun deviceIconEmoji(type: DeviceType): String = when (type) {
    DeviceType.LIGHT -> ""
    DeviceType.OUTLET -> ""
    DeviceType.SWITCH_PANEL -> ""
    DeviceType.IRON -> ""
    DeviceType.CAMERA -> ""
    DeviceType.LOCK -> ""
    DeviceType.THERMOSTAT -> ""
}

fun deviceStatusLabel(type: DeviceType): String = when (type) {
    DeviceType.OUTLET -> "Outlet"
    DeviceType.SWITCH_PANEL -> "Switch"
    DeviceType.IRON -> "Iron"
    DeviceType.LIGHT -> "Bulb"
    DeviceType.CAMERA -> "Camera"
    DeviceType.LOCK -> "Lock"
    DeviceType.THERMOSTAT -> "Thermostat"
}
