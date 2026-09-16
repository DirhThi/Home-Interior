package com.interiordesign3d.ui.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/** Every destination the root back stack can hold. */
@Parcelize
@Serializable
sealed class Dest : NavKey, Parcelable {

    /** Brand moment while Koin and the database come up; decides where to go next. */
    @Parcelize
    @Serializable
    data object ScrSplash : Dest()

    /** Language picker. [fromSettings] gives it a back button and returns instead of going on. */
    @Parcelize
    @Serializable
    data class ScrLanguage(val fromSettings: Boolean = false) : Dest()

    /** Asked once, purely to learn what people open this for. */
    @Parcelize
    @Serializable
    data object ScrSelect : Dest()

    /** Three pages, shown once. */
    @Parcelize
    @Serializable
    data object ScrOnboard : Dest()

    /** The tabbed shell. Its own tabs live in [DestMain], on a back stack of their own. */
    @Parcelize
    @Serializable
    data class ScrMain(val tab: DestMain = DestMain.ScrHome) : Dest()

    /** The editor. Pushed over the shell rather than living in a tab: it owns the whole screen. */
    @Parcelize
    @Serializable
    data class ScrDesigner(val roomId: String) : Dest()

    /** Browsing furniture with no room open. Reached from Home, so it covers the tab bar. */
    @Parcelize
    @Serializable
    data object ScrCatalogue : Dest()

    /** One catalogue item, with a 3D preview of just that model. */
    @Parcelize
    @Serializable
    data class ScrCatalogueItem(val key: String) : Dest()
}

/** Tabs of [Dest.ScrMain]. */
@Parcelize
@Serializable
sealed class DestMain : NavKey, Parcelable {

    @Parcelize
    @Serializable
    data object ScrHome : DestMain()

    @Parcelize
    @Serializable
    data object ScrProject : DestMain()

    @Parcelize
    @Serializable
    data object ScrSettings : DestMain()
}
