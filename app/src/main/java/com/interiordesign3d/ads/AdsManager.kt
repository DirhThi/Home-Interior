package com.interiordesign3d.ads

import android.content.Context
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.ui.navigation.Dest

/**
 * One place for every ad/tracking touchpoint the first-open funnel needs — where each screen goes
 * next, and when to load or show a native/interstitial ad. Every ad call here is a no-op today;
 * wiring a real SDK in later means filling these in, not restructuring the screens that call them.
 */
class AdsManager {
    var firstOpenDone: Boolean = false
        private set

    fun markFirstOpenDone() {
        firstOpenDone = true
    }

    fun nextSplash(): Dest = if (AppPrefs.onboarded) Dest.ScrMain() else Dest.ScrLanguage
    fun nextLanguage(): Dest = Dest.ScrOnboard
    fun nextOnboard(): Dest = Dest.ScrSelect

    fun loadNativeLanguageAlt(context: Context) {}
    fun loadNativeNextLanguage(context: Context) {}
    fun loadNativeNextOnboard(context: Context) {}
    fun preloadInterAds() {}

    /** A screen leaving the app (Settings → external link, share, …) calls this first. */
    fun disableAdResumeOnTime() {}
}
