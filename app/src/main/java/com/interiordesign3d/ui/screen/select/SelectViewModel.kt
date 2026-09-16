package com.interiordesign3d.ui.screen.select

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.data.repository.SpaceKind
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.select.state.SelectState

class SelectViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    val screenState: SelectState = object : SelectState() {
        override fun onContinue() {
            picked?.let { AppPrefs.recordSpaceKind(it) }
            done()
        }

        override fun onSkip() = done()
    }

    private fun done() =
        navigateTo(Dest.ScrMain(), popupTos = listOf(Dest.ScrSelect::class.java))
}
