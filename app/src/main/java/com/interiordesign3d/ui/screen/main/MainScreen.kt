package com.interiordesign3d.ui.screen.main

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.interiordesign3d.R
import com.interiordesign3d.common.utils.NavigationUtil.navigateTo
import com.interiordesign3d.ui.navigation.DestMain
import com.interiordesign3d.ui.properties.GlassTab
import com.interiordesign3d.ui.properties.GlassTabBar
import com.interiordesign3d.ui.properties.GlassTabBarHeight

/** Room the tab bar leaves at the bottom of every tab's content. */
val MainBottomInset = GlassTabBarHeight + 16.dp

enum class MainTab(@param:StringRes val title: Int, val icon: ImageVector, val route: DestMain) {
    Home(R.string.tab_home, Icons.Outlined.Home, DestMain.ScrHome),
    Project(R.string.tab_project, Icons.Outlined.Weekend, DestMain.ScrProject),
    Settings(R.string.tab_settings, Icons.Outlined.Tune, DestMain.ScrSettings),
}

/**
 * The tabbed shell. The designer is deliberately not a tab: it is pushed onto the root back stack and
 * takes the whole screen, because it carries a bar of its own and two stacked bars is worse than one.
 */
@Composable
fun MainScreen(mainBackStack: NavBackStack<NavKey>, startTab: DestMain = DestMain.ScrHome) {
    val tabBackStack = rememberNavBackStack(startTab)
    val selectedTab by remember {
        derivedStateOf { tabBackStack.lastOrNull() as? DestMain ?: startTab }
    }
    val selectedIndex = MainTab.entries.indexOfFirst { it.route == selectedTab }.coerceAtLeast(0)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MainNavigation(mainBackStack = mainBackStack, tabBackStack = tabBackStack)

        // No backdrop here on purpose: the tab screens are flat, so a blur shows nothing, and
        // sharing one recorded layer across three tabs left a ghost of the indicator's old position.
        GlassTabBar(
            tabs = MainTab.entries.map { GlassTab(stringResource(it.title), it.icon) },
            selectedIndex = selectedIndex,
            onSelect = { tabBackStack.navigateTo(MainTab.entries[it].route) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
        )
    }
}
