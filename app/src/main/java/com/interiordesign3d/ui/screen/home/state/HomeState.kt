package com.interiordesign3d.ui.screen.home.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.plans.SamplePlan

/**
 * A room plus its decoded plan. The card reads its size, room count and storeys off the plan — the
 * entity does not carry them, because a copy of them only ever goes stale.
 */
@Stable
data class RoomListItem(val room: DesignRoom, val plan: FloorPlan)

@Stable
open class HomeState : BaseScreenState() {
    var rooms by mutableStateOf<List<RoomListItem>>(emptyList())
    var showCredits by mutableStateOf(false)
    var showPlans by mutableStateOf(false)

    open fun onCreateRoom() {}
    open fun onOpenRoom(roomId: String) {}
    open fun onDeleteRoom(room: DesignRoom) {}
    open fun onShowPlans() { showPlans = true }
    open fun onDismissPlans() { showPlans = false }
    open fun onCreateFromPlan(plan: SamplePlan) {}
    open fun onShowCredits() { showCredits = true }
    open fun onDismissCredits() { showCredits = false }
}
