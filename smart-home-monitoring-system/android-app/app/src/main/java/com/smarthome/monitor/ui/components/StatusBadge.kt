package com.smarthome.monitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarthome.monitor.core.util.DeviceStatus

@Composable
fun StatusBadge(status: DeviceStatus, modifier: Modifier = Modifier) {
    val (containerColor, contentColor, label) = when (status) {
        DeviceStatus.ON ->
            Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "ON")
        DeviceStatus.OFF ->
            Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, "OFF")
        DeviceStatus.ERROR ->
            Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "ERROR")
        DeviceStatus.DISCONNECTED ->
            Triple(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.outline, "DISCONNECTED")
    }
    Text(
        text = label,
        color = contentColor,
        fontSize = 11.sp,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
