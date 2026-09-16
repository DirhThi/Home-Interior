package com.interiordesign3d.ui.screen.language

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.interiordesign3d.common.utils.NavigationUtil.popLast
import com.interiordesign3d.ui.navigation.DestLanguage
import com.interiordesign3d.ui.navigation.NavTransition
import com.interiordesign3d.ui.screen.language.alternative.LanguageAltScreen
import com.interiordesign3d.ui.screen.language.normal.LanguageScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The first-open Language step's own tiny back stack — [DestLanguage.ScrLanguage] then
 * [DestLanguage.ScrLanguageAlt], always in that order, with no visible transition. One
 * [LanguageViewModel] backs both.
 */
@Composable
fun LanguageNavigation(backStack: NavBackStack<NavKey>) {
    val viewModel: LanguageViewModel = koinViewModel { parametersOf(backStack) }

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
            entry<DestLanguage.ScrLanguage> { LanguageScreen(viewModel = viewModel) }
            entry<DestLanguage.ScrLanguageAlt> { LanguageAltScreen(viewModel = viewModel) }
        },
    )
}
