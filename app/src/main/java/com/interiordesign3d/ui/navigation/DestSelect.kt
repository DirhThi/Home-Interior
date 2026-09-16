package com.interiordesign3d.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * The Select step's own child back stack. [ScrSelectAlt] is not a variant to pick between — every
 * pick on [ScrSelect] pushes it, with no visible transition, purely so the step has a second
 * screen of its own to carry an ad slot and a tracking event.
 */
sealed class DestSelect : NavKey {
    data object ScrSelect : DestSelect()
    data object ScrSelectAlt : DestSelect()
}
