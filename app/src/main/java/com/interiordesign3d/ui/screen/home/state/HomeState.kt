package com.interiordesign3d.ui.screen.home.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState
import com.interiordesign3d.data.models.DesignRoom

@Stable
open class HomeState : BaseScreenState() {
    var rooms by mutableStateOf<List<DesignRoom>>(emptyList())
    var showCredits by mutableStateOf(false)

    open fun onCreateRoom() {}
    open fun onOpenRoom(roomId: String) {}
    open fun onDeleteRoom(room: DesignRoom) {}
    open fun onShowCredits() { showCredits = true }
    open fun onDismissCredits() { showCredits = false }
}
