package com.interiordesign3d.ui.screen.onboard

import com.interiordesign3d.data.onboard.OnboardConfig
import com.interiordesign3d.data.onboard.OnboardType

/** One page the pager actually shows — a real intro page, or an ad slot inserted between them. */
sealed interface OnboardSlot {
    val placement: String
    val trackingName: String

    data class Page(val type: OnboardType, val position: Int, override val placement: String) : OnboardSlot {
        override val trackingName: String get() = type.trackingName
    }

    data class Ad(override val placement: String, val key: String) : OnboardSlot {
        override val trackingName: String get() = OnboardConfig.screenOf(key)
    }
}
