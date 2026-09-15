package com.interiordesign3d.ui.screen.project

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.lifecycle.viewModelScope
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.plans.SamplePlan
import com.interiordesign3d.data.plans.readSamplePlanJson
import com.interiordesign3d.data.repository.AppDatabase
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.project.state.ProjectState
import com.interiordesign3d.ui.screen.project.state.RoomListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

class ProjectViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    private val db = AppDatabase.getInstance(app)

    val screenState: ProjectState = object : ProjectState() {
        override fun onCreateRoom() = createRoom(null)
        override fun onCreateFromPlan(plan: SamplePlan) = createRoom(plan)
        override fun onOpenRoom(roomId: String) = navigateTo(Dest.ScrDesigner(roomId))
        override fun onDeleteRoom(room: DesignRoom) = deleteRoom(room)
    }

    init {
        screenState.loading = true
        viewModelScope.launch {
            // Decoding is keyed on the JSON itself: moving one chair used to re-parse every room's
            // plan, because the furniture flow re-emits and this block reruns.
            val planCache = HashMap<String, FloorPlan>()
            combine(
                db.roomDao().getAllRooms(),
                db.placedFurnitureDao().getAllFurniture(),
            ) { rooms, furniture ->
                val byRoom = furniture.groupBy { it.roomId }
                planCache.keys.retainAll(rooms.map { it.floorPlanJson }.toSet())
                rooms.map { room ->
                    val plan = planCache.getOrPut(room.floorPlanJson) { room.plan() }
                    RoomListItem(room, plan, byRoom[room.id].orEmpty())
                }
            }
                .flowOn(Dispatchers.Default)
                .collect { items ->
                    screenState.rooms = items
                    screenState.loading = false
                }
        }
    }

    private fun createRoom(plan: SamplePlan?) {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            val planJson = plan?.let { readSamplePlanJson(app, it) }.orEmpty()
            db.roomDao().insertRoom(
                DesignRoom(
                    id = id,
                    name = plan?.label ?: app.getString(R.string.new_room),
                    heightCm = 260f,
                    floorPlanJson = planJson,
                )
            )
            screenState.showPlans = false
            navigateTo(Dest.ScrDesigner(id))
        }
    }

    /** A plan the card can read. A row written by a newer build may not decode — show it as empty. */
    private fun DesignRoom.plan(): FloorPlan =
        floorPlanJson.takeIf { it.isNotBlank() }
            ?.let { runCatching { Json.decodeFromString<FloorPlan>(it) }.getOrNull() }
            ?: FloorPlan()

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
