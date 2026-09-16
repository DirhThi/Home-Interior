package com.interiordesign3d.data.onboard

/** One slot in the onboarding pager: an intro page, or (once [isAd]) an ad placement between them. */
data class OnboardSlotConfig(val key: String, val adPlacement: String) {
    val isAd: Boolean get() = OnboardConfig.isAdKey(key)
    val screen: String get() = OnboardConfig.screenOf(key)
}

/**
 * The onboarding pager's slot order — a flat local default today. This is the seam a remote
 * config would plug into later; the pager itself only ever reads the resulting slot list.
 */
object OnboardConfig {
    private const val AD_PREFIX = "ad_"

    fun isAdKey(key: String): Boolean = key.startsWith(AD_PREFIX)

    fun screenOf(key: String): String =
        if (isAdKey(key)) "ScrOnboardAd${key.removePrefix(AD_PREFIX)}"
        else OnboardType.fromSlotKey(key)?.trackingName ?: key

    val DEFAULT: List<OnboardSlotConfig> = listOf(
        OnboardSlotConfig("ob_plan", "onboard_1"),
        OnboardSlotConfig("ob_furnish", "onboard_2"),
        OnboardSlotConfig("ob_outside", "onboard_3"),
    )
}
