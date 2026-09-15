package com.interiordesign3d.ui.screen.home.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState

@Stable
open class HomeState : BaseScreenState() {
    var projectCount by mutableIntStateOf(0)

    open fun onNewProject() {}
    open fun onExplore() {}
}
