package com.interiordesign3d.ui.screen.onboard

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.ads.AdsManager
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.onboard.OnboardConfig
import com.interiordesign3d.data.onboard.OnboardType
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.ui.navigation.Dest

class OnboardViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
    private val adsManager: AdsManager,
) : BaseViewModel(app, backStack) {

    val slots: List<OnboardSlot> = buildSlots()

    private var navigated = false

    private fun buildSlots(): List<OnboardSlot> {
        var position = 0
        return OnboardConfig.DEFAULT.mapNotNull { cfg ->
            if (cfg.isAd) OnboardSlot.Ad(cfg.adPlacement, cfg.key)
            else OnboardType.fromSlotKey(cfg.key)?.let { OnboardSlot.Page(it, position++, cfg.adPlacement) }
        }
    }

    fun preloadNextScreen() = adsManager.loadNativeNextOnboard(app)

    fun onFinish() {
        if (navigated) return
        navigated = true
        AppPrefs.markOnboarded()
        navigateTo(adsManager.nextOnboard(), popupTos = listOf(Dest.ScrOnboard::class.java))
    }
}
