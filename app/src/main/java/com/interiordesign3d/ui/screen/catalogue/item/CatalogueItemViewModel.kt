package com.interiordesign3d.ui.screen.catalogue.item

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.catalog.catalogItem
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.repository.AppDatabase
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.catalogue.item.state.CatalogueItemState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class CatalogueItemViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
    private val key: String,
) : BaseViewModel(app, backStack) {

    private val db = AppDatabase.getInstance(app)

    val screenState: CatalogueItemState = object : CatalogueItemState() {

        override fun onBack() = pops()

        override fun onAddToRoom(roomId: String) {
            showRoomPicker = false
            addTo(roomId)
        }

        override fun onCreateRoom() {
            showRoomPicker = false
            createRoomThenAdd()
        }
    }

    init {
        screenState.item = catalogItem(key)
        viewModelScope.launch {
            screenState.rooms = db.roomDao().getAllRooms().first()
        }
    }

    /** Drops the item at the room's origin; the designer is where it actually gets placed. */
    private fun addTo(roomId: String) {
        val item = screenState.item ?: return
        viewModelScope.launch {
            db.placedFurnitureDao().insertPlacedFurniture(
                PlacedFurniture(
                    id = UUID.randomUUID().toString(),
                    roomId = roomId,
                    furnitureId = item.key,
                    furnitureName = item.label,
                    modelUrl = item.model,
                    isWallMounted = item.wallMounted,
                    wallMountHeight = item.wallHeightCm,
                )
            )
            navigateTo(Dest.ScrDesigner(roomId))
        }
    }

    private fun createRoomThenAdd() {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            db.roomDao().insertRoom(
                DesignRoom(
                    id = id,
                    name = app.getString(R.string.new_room),
                    heightCm = 260f,
                )
            )
            addTo(id)
        }
    }
}
