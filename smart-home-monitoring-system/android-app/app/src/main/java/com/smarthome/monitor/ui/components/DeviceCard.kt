package com.smarthome.monitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarthome.monitor.core.util.status
import com.smarthome.monitor.data.model.Device

@Composable
fun DeviceCard(device: Device, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = device.name, fontSize = 13.sp, maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = deviceTypeIcon(device.type), fontSize = 20.sp)
                StatusBadge(status = device.status())
            }
        }
    }
}

private fun deviceTypeIcon(type: com.smarthome.monitor.data.model.DeviceType): String = when (type) {
    com.smarthome.monitor.data.model.DeviceType.LIGHT -> "\uD83D\uDCA1"
    com.smarthome.monitor.data.model.DeviceType.OUTLET -> "\uD83D\uDD0C"
    com.smarthome.monitor.data.model.DeviceType.IRON -> "\uD83E\uDDF4"
    com.smarthome.monitor.data.model.DeviceType.SWITCH_PANEL -> "\uD83D\uDD0B"
    com.smarthome.monitor.data.model.DeviceType.CAMERA -> "\uD83D\uDCF7"
    com.smarthome.monitor.data.model.DeviceType.LOCK -> "\uD83D\uDD12"
    com.smarthome.monitor.data.model.DeviceType.THERMOSTAT -> "\uD83C\uDF21\uFE0F"
}