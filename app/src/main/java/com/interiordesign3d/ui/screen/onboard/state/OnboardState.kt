package com.interiordesign3d.ui.screen.onboard.state

import androidx.compose.runtime.Stable
import com.interiordesign3d.common.base.BaseScreenState

@Stable
open class OnboardState : BaseScreenState() {
    open fun onFinish() {}
}
