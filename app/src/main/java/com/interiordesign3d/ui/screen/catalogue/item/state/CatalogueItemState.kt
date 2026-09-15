package com.interiordesign3d.ui.screen.catalogue.item.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState
import com.interiordesign3d.data.catalog.CatalogItem
import com.interiordesign3d.data.models.DesignRoom

@Stable
open class CatalogueItemState : BaseScreenState() {
    var item by mutableStateOf<CatalogItem?>(null)
    var rooms by mutableStateOf<List<DesignRoom>>(emptyList())
    var showRoomPicker by mutableStateOf(false)

    val hasRooms: Boolean get() = rooms.isNotEmpty()

    open fun onBack() {}
    open fun onAddTapped() { showRoomPicker = true }
    open fun onDismissRoomPicker() { showRoomPicker = false }
    open fun onAddToRoom(roomId: String) {}
    open fun onCreateRoom() {}
}
