package com.interiordesign3d.data.onboard

import androidx.annotation.StringRes
import com.interiordesign3d.R

/** One intro page's static content — icon, title, body — keyed by [slotKey] for [OnboardConfig]. */
enum class OnboardType(
    val slotKey: String,
    val trackingName: String,
    @StringRes val title: Int,
    @StringRes val body: Int,
) {
    Ob1("ob_plan", "ScrOnboard1", R.string.ob1_title, R.string.ob1_body),
    Ob2("ob_furnish", "ScrOnboard2", R.string.ob2_title, R.string.ob2_body),
    Ob3("ob_outside", "ScrOnboard3", R.string.ob3_title, R.string.ob3_body);

    companion object {
        fun fromSlotKey(key: String): OnboardType? = entries.firstOrNull { it.slotKey == key }
    }
}
