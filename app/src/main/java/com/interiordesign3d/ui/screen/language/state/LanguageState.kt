package com.interiordesign3d.ui.screen.language.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
open class LanguageState {
    /** Chosen but not applied — the list is browsed, then confirmed. */
    var picked by mutableStateOf("en")

    /** Tapping an already-picked language again on the Alt step asks for confirmation instead. */
    var showConfirmLanguage by mutableStateOf(false)
    var confirmCode by mutableStateOf("")

    open fun onPick(code: String) { picked = code }
    open fun onConfirm() {}
    open fun onBack() {}
    open fun onConfirmLanguageDismiss() { showConfirmLanguage = false }
    open fun onConfirmLanguageAccept() {}
}
