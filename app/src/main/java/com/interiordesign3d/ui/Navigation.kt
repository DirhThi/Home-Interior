package com.interiordesign3d.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.interiordesign3d.common.base.Navigator
import com.interiordesign3d.ui.screen.designer.DesignerScreen
import com.interiordesign3d.ui.screen.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object RoomDesigner : Screen("room_designer/{roomId}") {
        fun createRoute(roomId: String) = "room_designer/$roomId"
    }
}

@Composable
fun InteriorDesignNavHost() {
    val navController = rememberNavController()
    val navigator = rememberNavigator(navController)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(280)) { it / 5 } },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(280)) { -it / 5 } },
        popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(280)) { it / 5 } },
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navigator = navigator)
        }

        composable(
            route = Screen.RoomDesigner.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
        ) { entry ->
            DesignerScreen(
                roomId = entry.arguments?.getString("roomId").orEmpty(),
                navigator = navigator,
            )
        }
    }
}

@Composable
private fun rememberNavigator(controller: NavHostController): Navigator =
    remember(controller) {
        object : Navigator {
            override fun to(route: String) {
                controller.navigate(route)
            }

            override fun back() {
                controller.popBackStack()
            }
        }
    }
