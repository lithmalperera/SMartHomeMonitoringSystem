package com.smarthome.monitor.ui.settings

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
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar("Settings saved")
            viewModel.resetSavedFlag()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("\u2190") } }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        var limitText by remember(uiState.defaultSafetyLimit) {
            mutableStateOf(uiState.defaultSafetyLimit.toString())
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.homeName,
                onValueChange = { viewModel.setHomeName(it) },
                label = { Text("Home name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notifications")
                    Text(
                        "FCM topic: home_home_001",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            HorizontalDivider()

            Text("Default iron safety limit")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                    label = { Text("maxActiveMinutes") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { limitText.toIntOrNull()?.let { viewModel.setSafetyLimit(it) } }) {
                    Text("Set")
                }
            }

            HorizontalDivider()

            Text("About", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Smart Home Monitoring System v1.0", fontSize = 12.sp)
            Text("Campus Mini Project · Anonymous Auth · Firebase RTDB", fontSize = 12.sp)

            Spacer(Modifier.weight(1f))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}