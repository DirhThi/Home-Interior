package com.interiordesign3d.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.interiordesign3d.common.utils.NavigationUtil.popLast
import com.interiordesign3d.ui.screen.catalogue.CatalogueScreen
import com.interiordesign3d.ui.screen.catalogue.CatalogueViewModel
import com.interiordesign3d.ui.screen.catalogue.item.CatalogueItemScreen
import com.interiordesign3d.ui.screen.catalogue.item.CatalogueItemViewModel
import com.interiordesign3d.ui.screen.designer.DesignerScreen
import com.interiordesign3d.ui.screen.designer.DesignerViewModel
import com.interiordesign3d.ui.screen.main.MainScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Dest.ScrMain())

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        modifier = modifier,
        onBack = { backStack.popLast() },
        popTransitionSpec = NavTransition.pop,
        predictivePopTransitionSpec = NavTransition.predictivePop,
        entryProvider = entryProvider {
            entry<Dest.ScrMain> { dest ->
                MainScreen(mainBackStack = backStack, startTab = dest.tab)
            }

            entry<Dest.ScrDesigner> { dest ->
                val vm: DesignerViewModel =
                    koinViewModel(key = "designer_${dest.roomId}") {
                        parametersOf(backStack, dest.roomId)
                    }
                DesignerScreen(vm)
            }

            entry<Dest.ScrCatalogue> {
                val vm: CatalogueViewModel = koinViewModel { parametersOf(backStack) }
                CatalogueScreen(vm)
            }

            entry<Dest.ScrCatalogueItem> { dest ->
                val vm: CatalogueItemViewModel =
                    koinViewModel(key = "catalogue_${dest.key}") {
                        parametersOf(backStack, dest.key)
                    }
                CatalogueItemScreen(vm)
            }
        },
    )
}
