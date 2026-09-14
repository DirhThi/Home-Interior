package com.interiordesign3d.ui.screen.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.common.base.Navigator
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.repository.AppDatabase
import com.interiordesign3d.ui.Screen
import com.interiordesign3d.ui.screen.home.state.HomeState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(app: Application, navigator: Navigator) : BaseViewModel(app, navigator) {

    private val db = AppDatabase.getInstance(app)

    val screenState: HomeState = object : HomeState() {
        override fun onCreateRoom() = createRoom()
        override fun onOpenRoom(roomId: String) = navigateTo(Screen.RoomDesigner.createRoute(roomId))
        override fun onDeleteRoom(room: DesignRoom) = deleteRoom(room)
    }

    init {
        screenState.loading = true
        viewModelScope.launch {
            db.roomDao().getAllRooms().collect { rooms ->
                screenState.rooms = rooms
                screenState.loading = false
            }
        }
    }

    private fun createRoom() {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            db.roomDao().insertRoom(
                DesignRoom(
                    id = id,
                    name = app.getString(R.string.new_room),
                    widthCm = 380f,
                    lengthCm = 520f,
                    heightCm = 260f,
                )
            )
            navigateTo(Screen.RoomDesigner.createRoute(id))
        }
    }

    /** Deletes the room and its furniture, keeping both around so the snackbar can put them back. */
    private fun deleteRoom(room: DesignRoom) {
        viewModelScope.launch {
            val furniture: List<PlacedFurniture> =
                db.placedFurnitureDao().getFurnitureForRoom(room.id).first()
            db.placedFurnitureDao().clearRoomFurniture(room.id)
            db.roomDao().deleteRoom(room)
            notify(
                text = app.getString(R.string.room_deleted, room.name),
                actionLabel = app.getString(R.string.undo),
                onAction = { restore(room, furniture) },
            )
        }
    }

    private fun restore(room: DesignRoom, furniture: List<PlacedFurniture>) {
        viewModelScope.launch {
            db.roomDao().insertRoom(room)
            furniture.forEach { db.placedFurnitureDao().insertPlacedFurniture(it) }
        }
    }
}
