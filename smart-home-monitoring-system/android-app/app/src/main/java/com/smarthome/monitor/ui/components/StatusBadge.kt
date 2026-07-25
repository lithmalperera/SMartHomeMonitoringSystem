package com.smarthome.monitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarthome.monitor.core.util.DeviceStatus

@Composable
fun StatusBadge(status: DeviceStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        DeviceStatus.ON -> Color(0xFF4CAF50) to "ON"
        DeviceStatus.OFF -> Color(0xFF9E9E9E) to "OFF"
        DeviceStatus.ERROR -> Color(0xFFF44336) to "ERROR"
        DeviceStatus.DISCONNECTED -> Color(0xFF607D8B) to "DISCONNECTED"
    }
    Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}