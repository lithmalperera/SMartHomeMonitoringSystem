package com.smarthome.monitor.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.ui.components.EmptyState
import com.smarthome.monitor.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit, viewModel: ReportsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = { TextButton(onClick = onBack) { Text("\u2190") } }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DateSelector(
                    date = uiState.date,
                    onPrev = { viewModel.shiftDate(-1) },
                    onNext = { viewModel.shiftDate(1) }
                )
            }

            if (uiState.rows.isEmpty()) {
                item { EmptyState("No usage data for this day", modifier = Modifier.fillMaxWidth()) }
            } else {
                item {
                    Text("Totals", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TotalsRow(uiState.totals)
                }

                item {
                    Text("Active minutes (bar chart)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    UsageBarChart(rows = uiState.rows)
                }

                item {
                    Text("Per device", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                }

                items(uiState.rows) { row -> UsageRowCard(row) }
            }
        }
    }
}

@Composable
private fun DateSelector(date: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day") }
        Text(date, fontSize = 16.sp)
        IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next day") }
    }
}

@Composable
private fun TotalsRow(totals: Totals) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatColumn("Active", "${totals.activeMinutes}m")
        StatColumn("Sessions", "${totals.sessions}")
        StatColumn("Energy", "%.1f Wh".format(totals.energyWh))
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun UsageBarChart(rows: List<DeviceUsageRow>, modifier: Modifier = Modifier) {
    val maxMinutes = (rows.maxOfOrNull { it.record.activeMinutes } ?: 1).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val gapRatio = 0.2f
            val slot = size.width / rows.size
            val barWidth = slot * (1f - gapRatio)
            rows.forEachIndexed { index, row ->
                val full = row.record.activeMinutes.toFloat() / maxMinutes
                val barHeight = size.height * full
                val x = slot * index + (slot - barWidth) / 2
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            rows.forEach { Text(it.device.name, fontSize = 10.sp, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun UsageRowCard(row: DeviceUsageRow) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.device.name, fontSize = 15.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val r = row.record
                Text("Active: ${r.activeMinutes}m", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sessions: ${r.sessions}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1f Wh".format(r.energyWh), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}