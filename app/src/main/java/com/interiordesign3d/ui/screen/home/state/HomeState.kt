package com.interiordesign3d.ui.screen.home.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.plans.SamplePlan

/**
 * A room plus everything the card draws: its decoded plan and what is standing in it. The entity
 * carries none of this, because a copy of it only ever goes stale.
 */
@Stable
data class RoomListItem(
    val room: DesignRoom,
    val plan: FloorPlan,
    val furniture: List<PlacedFurniture> = emptyList(),
)

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
