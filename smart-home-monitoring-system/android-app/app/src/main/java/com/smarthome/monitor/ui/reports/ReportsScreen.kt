package com.smarthome.monitor.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit, viewModel: ReportsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }, navigationIcon = {
        TextButton(onClick = onBack) { Text("\u2190") }
    }) }) { padding ->
        Text(uiState.text, modifier = Modifier.padding(padding))
    }
}