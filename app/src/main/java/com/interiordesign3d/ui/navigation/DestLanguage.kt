package com.interiordesign3d.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * The first-open Language step's own child back stack. [ScrLanguageAlt] is not a variant to pick
 * between — picking a language on [ScrLanguage] pushes it, with no visible transition, purely so
 * the step has a second screen of its own to carry an ad slot and a tracking event.
 */
sealed class DestLanguage : NavKey {
    data object ScrLanguage : DestLanguage()
    data object ScrLanguageAlt : DestLanguage()
}
