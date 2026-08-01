package com.smarthome.monitor.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthome.monitor.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(deviceId: String, onBack: () -> Unit, viewModel: ScheduleViewModel = viewModel()) {
    LaunchedEffect(deviceId) { viewModel.load(deviceId) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar("Schedule saved")
            viewModel.resetSavedFlag()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                navigationIcon = { TextButton(onClick = onBack) { Text("\u2190") } }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        val schedule = uiState.schedule
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(schedule.deviceId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = schedule.onTime,
                onValueChange = { viewModel.setOnTime(it) },
                label = { Text("ON time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = schedule.offTime,
                onValueChange = { viewModel.setOffTime(it) },
                label = { Text("OFF time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enabled")
                Switch(checked = schedule.enabled, onCheckedChange = { viewModel.setEnabled(it) })
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}