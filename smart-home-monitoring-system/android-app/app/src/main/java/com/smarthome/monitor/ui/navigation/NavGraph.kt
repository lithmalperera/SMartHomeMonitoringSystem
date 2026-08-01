package com.smarthome.monitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smarthome.monitor.ui.alerts.AlertsScreen
import com.smarthome.monitor.ui.device.DeviceControlSheet
import com.smarthome.monitor.ui.device.DevicePlacementScreen
import com.smarthome.monitor.ui.floor.FloorScreen
import com.smarthome.monitor.ui.floor.FloorSetupScreen
import com.smarthome.monitor.ui.home.HomeScreen
import com.smarthome.monitor.ui.reports.ReportsScreen
import com.smarthome.monitor.ui.schedule.ScheduleScreen
import com.smarthome.monitor.ui.settings.SettingsScreen
import com.smarthome.monitor.ui.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onFloorSelected = { floorId -> navController.navigate(Routes.floor(floorId)) },
                onAddFloor = { navController.navigate(Routes.FLOOR_SETUP) },
                onAddDevice = { navController.navigate(Routes.DEVICE_PLACEMENT) },
                onAlertsClick = { navController.navigate(Routes.ALERTS) },
                onUsageClick = { navController.navigate(Routes.USAGE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.FLOOR,
            arguments = listOf(navArgument("floorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val floorId = backStackEntry.arguments?.getString("floorId") ?: return@composable
            FloorScreen(
                floorId = floorId,
                onDeviceClick = { deviceId -> navController.navigate(Routes.device(deviceId)) },
                onAddDevice = { navController.navigate(Routes.DEVICE_PLACEMENT) },
                onHomeClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onAlertsClick = { navController.navigate(Routes.ALERTS) },
                onUsageClick = { navController.navigate(Routes.USAGE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.FLOOR_SETUP) {
            FloorSetupScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(Routes.DEVICE_PLACEMENT) {
            DevicePlacementScreen(
                onClose = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DEVICE,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            DeviceControlSheet(
                deviceId = deviceId,
                onScheduleClick = { navController.navigate(Routes.schedule(deviceId)) },
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.SCHEDULE,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            ScheduleScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                onHomeClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onFloorClick = { navController.navigate(Routes.floor("floor_ground")) { popUpTo(Routes.HOME) } },
                onAlertsClick = { navController.navigate(Routes.ALERTS) },
                onUsageClick = { navController.navigate(Routes.USAGE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ALERTS) {
            AlertsScreen(
                onHomeClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onFloorClick = { navController.navigate(Routes.floor("floor_ground")) { popUpTo(Routes.HOME) } },
                onUsageClick = { navController.navigate(Routes.USAGE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.USAGE) {
            ReportsScreen(
                onHomeClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onFloorClick = { navController.navigate(Routes.floor("floor_ground")) { popUpTo(Routes.HOME) } },
                onAlertsClick = { navController.navigate(Routes.ALERTS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onHomeClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onFloorClick = { navController.navigate(Routes.floor("floor_ground")) { popUpTo(Routes.HOME) } },
                onAlertsClick = { navController.navigate(Routes.ALERTS) },
                onUsageClick = { navController.navigate(Routes.USAGE) }
            )
        }
    }
}
