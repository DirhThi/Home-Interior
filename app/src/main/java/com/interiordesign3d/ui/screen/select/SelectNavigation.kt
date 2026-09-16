package com.interiordesign3d.ui.screen.select

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.interiordesign3d.common.utils.NavigationUtil.popLast
import com.interiordesign3d.ui.navigation.DestSelect
import com.interiordesign3d.ui.navigation.NavTransition
import com.interiordesign3d.ui.screen.select.alternative.SelectAltScreen
import com.interiordesign3d.ui.screen.select.normal.SelectScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The Select step's own tiny back stack — [DestSelect.ScrSelect] then [DestSelect.ScrSelectAlt],
 * always in that order, with no visible transition. One [SelectViewModel] backs both.
 */
@Composable
fun SelectNavigation(backStack: NavBackStack<NavKey>) {
    val viewModel: SelectViewModel = koinViewModel { parametersOf(backStack) }

    NavDisplay(
        backStack = viewModel.backStackChild,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { viewModel.backStackChild.popLast() },
        transitionSpec = NavTransition.none,
        popTransitionSpec = NavTransition.pop,
        predictivePopTransitionSpec = NavTransition.predictivePop,
        entryProvider = entryProvider {
            entry<DestSelect.ScrSelect> { SelectScreen(viewModel = viewModel) }
            entry<DestSelect.ScrSelectAlt> { SelectAltScreen(viewModel = viewModel) }
        },
    )
}
