package com.interiordesign3d.ui.screen.language

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.ads.AdsManager
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.common.utils.NavigationUtil.navigateTo
import com.interiordesign3d.data.repository.LanguageManager
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.navigation.DestLanguage
import com.interiordesign3d.ui.screen.language.state.LanguageState

/**
 * Backs both [ui.screen.language.normal.LanguageScreen] and
 * [ui.screen.language.alternative.LanguageAltScreen] — one instance, one [backStackChild].
 */
class LanguageViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
    private val adsManager: AdsManager,
) : BaseViewModel(app, backStack) {

    /** The step's own back stack: [DestLanguage.ScrLanguage], then [DestLanguage.ScrLanguageAlt]. */
    val backStackChild = mutableStateListOf<NavKey>(DestLanguage.ScrLanguage)

    private val tappedCodes = mutableSetOf<String>()

    val screenState: LanguageState = object : LanguageState() {
        override fun onPick(code: String) {
            if (!isAltScreen) {
                picked = code
                goAlt()
                return
            }
            if (!tappedCodes.add(code)) {
                confirmCode = code
                showConfirmLanguage = true
                return
            }
            picked = code
        }

        override fun onConfirm() {
            if (isAltScreen) applyAndGoNext() else goAlt()
        }

        override fun onConfirmLanguageAccept() {
            showConfirmLanguage = false
            picked = confirmCode
            applyAndGoNext()
        }
    }

    init {
        LanguageManager.sync()
        screenState.picked = LanguageManager.current
    }

    private val isAltScreen: Boolean get() = backStackChild.lastOrNull() == DestLanguage.ScrLanguageAlt

    private fun goAlt() {
        tappedCodes.clear()
        tappedCodes.add(screenState.picked)
        backStackChild.navigateTo(DestLanguage.ScrLanguageAlt)
        adsManager.loadNativeNextLanguage(app)
    }

    private fun applyAndGoNext() {
        LanguageManager.apply(screenState.picked)
        navigateTo(adsManager.nextLanguage(), popupTos = listOf(Dest.ScrLanguage::class.java))
    }

    fun preloadAltAd() = adsManager.loadNativeLanguageAlt(app)
}
