package com.smarthome.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.smarthome.monitor.ui.navigation.NavGraph
import com.smarthome.monitor.ui.theme.SmartHomeMonitoringSystemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeMonitoringSystemTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}