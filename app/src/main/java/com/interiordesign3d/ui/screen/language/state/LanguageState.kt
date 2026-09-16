package com.interiordesign3d.ui.screen.language.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState

@Stable
open class LanguageState : BaseScreenState() {
    /** Chosen but not applied — the list is browsed, then confirmed. */
    var picked by mutableStateOf("en")

    /** True when this is the first-open pass; there is no going back from it. */
    var firstOpen by mutableStateOf(false)

    open fun onPick(code: String) { picked = code }
    open fun onConfirm() {}
    open fun onBack() {}
}
