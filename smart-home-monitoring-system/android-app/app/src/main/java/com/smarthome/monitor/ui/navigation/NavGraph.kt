package com.smarthome.monitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smarthome.monitor.ui.splash.SplashScreen
import com.smarthome.monitor.ui.home.HomeScreen
import com.smarthome.monitor.ui.floor.FloorScreen
import com.smarthome.monitor.ui.device.DeviceControlSheet
import com.smarthome.monitor.ui.schedule.ScheduleScreen
import com.smarthome.monitor.ui.reports.ReportsScreen
import com.smarthome.monitor.ui.camera.CameraScreen
import com.smarthome.monitor.ui.settings.SettingsScreen

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
                onFloorClick = { floorId -> navController.navigate(Routes.floor(floorId)) },
                onReportsClick = { navController.navigate(Routes.REPORTS) },
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
                onBack = { navController.popBackStack() }
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
                onCameraClick = { navController.navigate(Routes.camera(deviceId)) },
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.SCHEDULE,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            ScheduleScreen(deviceId = deviceId, onBack = { navController.popBackStack() })
        }
        composable(Routes.REPORTS) {
            ReportsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.CAMERA,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            CameraScreen(deviceId = deviceId, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}