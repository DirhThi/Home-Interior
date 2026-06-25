package com.interiordesign3d.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.interiordesign3d.ui.screens.ColorPickerScreen
import com.interiordesign3d.ui.screens.HomeScreen
import com.interiordesign3d.ui.screens.RoomDesignerScreen

// ─── Routes ───────────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object RoomDesigner : Screen("room_designer/{roomId}") {
        fun createRoute(roomId: String) = "room_designer/$roomId"
    }
    object ColorPicker  : Screen("color_picker/{roomId}") {
        fun createRoute(roomId: String) = "color_picker/$roomId"
    }
}

// ─── Nav host ─────────────────────────────────────────────────────────────────

@Composable
fun InteriorDesignNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route,
        enterTransition  = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 4 } },
        exitTransition   = { fadeOut(tween(200)) },
        popEnterTransition  = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -it / 4 } },
        popExitTransition   = { fadeOut(tween(200)) + slideOutHorizontally(tween(280)) { it / 4 } }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDesigner = { roomId ->
                    navController.navigate(Screen.RoomDesigner.createRoute(roomId))
                }
            )
        }

        composable(
            route     = Screen.RoomDesigner.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            RoomDesignerScreen(
                roomId                 = roomId,
                onNavigateToColorPicker = {
                    navController.navigate(Screen.ColorPicker.createRoute(roomId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.ColorPicker.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            ColorPickerScreen(
                roomId = roomId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
