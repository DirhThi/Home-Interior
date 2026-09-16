package com.interiordesign3d.ui.screen.onboard.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.interiordesign3d.ui.properties.AdSlot
import com.interiordesign3d.ui.screen.onboard.OnboardSlot

/** A full pager page that is nothing but an ad placement — [OnboardConfig] doesn't ship one yet. */
@Composable
fun OnboardAdContent(slot: OnboardSlot.Ad) {
    AdSlot(nameSpace = slot.placement, modifier = Modifier.fillMaxSize())
}
