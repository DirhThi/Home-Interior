package com.interiordesign3d.ui.screen.onboard

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.onboard.state.OnboardState

class OnboardViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    val screenState: OnboardState = object : OnboardState() {
        override fun onFinish() {
            AppPrefs.markOnboarded()
            navigateTo(Dest.ScrMain(), popupTos = listOf(Dest.ScrOnboard::class.java))
        }
    }
}
