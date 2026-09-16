package com.interiordesign3d.ui.screen.select.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState
import com.interiordesign3d.data.repository.SpaceKind

@Stable
open class SelectState : BaseScreenState() {
    var picked by mutableStateOf<SpaceKind?>(null)

    open fun onPick(kind: SpaceKind) { picked = kind }
    open fun onContinue() {}
    open fun onSkip() {}
}
