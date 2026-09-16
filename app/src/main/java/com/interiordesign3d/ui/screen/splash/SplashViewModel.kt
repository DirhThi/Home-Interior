package com.interiordesign3d.ui.screen.splash

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.splash.state.SplashState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Long enough to read the name, short enough not to be a toll gate. */
private const val HOLD_MS = 900L

class SplashViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    val screenState: SplashState = object : SplashState() {}

    private var navigated = false

    init {
        viewModelScope.launch {
            delay(HOLD_MS)
            next()
        }
    }

    private fun next() {
        if (navigated) return
        navigated = true
        val target = if (AppPrefs.onboarded) Dest.ScrMain() else Dest.ScrOnboard
        navigateTo(target, popupTos = listOf(Dest.ScrSplash::class.java))
    }
}
