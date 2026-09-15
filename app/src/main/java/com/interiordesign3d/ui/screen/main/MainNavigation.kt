package com.interiordesign3d.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.interiordesign3d.common.utils.NavigationUtil.popLast
import com.interiordesign3d.ui.navigation.DestMain
import com.interiordesign3d.ui.navigation.NavTransition
import com.interiordesign3d.ui.screen.home.HomeScreen
import com.interiordesign3d.ui.screen.home.HomeViewModel
import com.interiordesign3d.ui.screen.project.ProjectScreen
import com.interiordesign3d.ui.screen.project.ProjectViewModel
import com.interiordesign3d.ui.screen.settings.SettingsScreen
import com.interiordesign3d.ui.screen.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The tabs' own NavDisplay. Tab screens navigate on the *root* stack, so detail screens cover the bar. */
@Composable
fun MainNavigation(
    mainBackStack: NavBackStack<NavKey>,
    tabBackStack: NavBackStack<NavKey>,
) {
    Box(Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = tabBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            modifier = Modifier.fillMaxSize(),
            onBack = { tabBackStack.popLast() },
            popTransitionSpec = NavTransition.pop,
            predictivePopTransitionSpec = NavTransition.predictivePop,
            entryProvider = entryProvider {
                entry<DestMain.ScrHome> {
                    val vm: HomeViewModel = koinViewModel(parameters = { parametersOf(mainBackStack) })
                    HomeScreen(vm)
                }
                entry<DestMain.ScrProject> {
                    val vm: ProjectViewModel = koinViewModel(parameters = { parametersOf(mainBackStack) })
                    ProjectScreen(vm)
                }
                entry<DestMain.ScrSettings> {
                    val vm: SettingsViewModel = koinViewModel(parameters = { parametersOf(mainBackStack) })
                    SettingsScreen(vm)
                }
            },
        )
    }
}
