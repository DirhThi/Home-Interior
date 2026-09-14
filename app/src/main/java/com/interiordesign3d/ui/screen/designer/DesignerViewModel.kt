package com.interiordesign3d.ui.screen.designer

import android.app.Application
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.common.base.Navigator
import com.interiordesign3d.data.catalog.catalogItem
import com.interiordesign3d.data.models.ColorPalette
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.OpeningType
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.models.Stair
import com.interiordesign3d.data.models.StairShape
import com.interiordesign3d.data.models.WallOpening
import com.interiordesign3d.data.models.WallPoint
import com.interiordesign3d.data.repository.AppDatabase
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val AUTO_SAVE_DELAY_MS = 400L

class DesignerViewModel(
    app: Application,
    navigator: Navigator,
    private val roomId: String,
) : BaseViewModel(app, navigator) {

    private val db = AppDatabase.getInstance(app)
    private var loaded = false

    val screenState: DesignerState = object : DesignerState() {

        override fun onBack() = pops()

        override fun onSave() {
            viewModelScope.launch {
                persistPlan()
                persistFurniture(placedFurniture)
                notify(app.getString(R.string.saved))
            }
        }

        override fun onEditFloorPlan() {
            viewModelScope.launch {
                persistFurniture(placedFurniture)
                editorMode = EditorMode.DRAW_WALLS
            }
        }

        override fun onEnterDesign() {
            viewModelScope.launch {
                persistPlan()
                editorMode = EditorMode.DESIGN
            }
        }

        // ── Wall drawing ──────────────────────────────────────────────────────

        override fun onAddNewPoint(point: WallPoint) {
            val index = floorPlan.nodes.size
            floorPlan = floorPlan.copy(nodes = floorPlan.nodes + point)
            currentPath = currentPath + index
        }

        override fun onSnapToNode(index: Int) {
            currentPath = currentPath + index
        }

        override fun onClosePath() {
            if (currentPath.size >= 3) {
                floorPlan = floorPlan.addRoom(currentPath, activeLevel)
                drawingPhase = DrawingPhase.CLOSED
            }
        }

        override fun onMoveNode(index: Int, point: WallPoint) {
            floorPlan = floorPlan.copy(
                nodes = floorPlan.nodes.toMutableList().also { it[index] = point }
            )
        }

        override fun onStartFromNode(index: Int) {
            currentPath = listOf(index)
            drawingPhase = DrawingPhase.PLACING
        }

        override fun onStartNewPoint(point: WallPoint) {
            val index = floorPlan.nodes.size
            floorPlan = floorPlan.copy(nodes = floorPlan.nodes + point)
            currentPath = listOf(index)
            drawingPhase = DrawingPhase.PLACING
        }

        override fun onDone() {
            currentPath = emptyList()
            drawingPhase = DrawingPhase.EDITING
        }

        override fun onUndo() = undoLastStep()

        override fun onClear() {
            floorPlan = FloorPlan()
            currentPath = emptyList()
            drawingPhase = DrawingPhase.PLACING
        }

        // ── Openings ──────────────────────────────────────────────────────────

        override fun onPlaceOpening(roomIdx: Int, edgeIdx: Int, t: Float, type: OpeningType) {
            // The canvas reports the room and edge it was tapped on; the opening is stored against
            // the wall's node pair so it cuts that wall once, for every room touching it.
            val room = floorPlan.rooms.getOrNull(roomIdx) ?: return
            val nodeA = room.getOrNull(edgeIdx) ?: return
            val nodeB = room.getOrNull((edgeIdx + 1) % room.size) ?: return
            val opening = WallOpening(
                id = UUID.randomUUID().toString(),
                nodeA = nodeA, nodeB = nodeB, level = activeLevel, t = t, type = type,
                widthCm = if (type == OpeningType.DOOR) DEFAULT_DOOR_CM else DEFAULT_WINDOW_CM,
            )
            floorPlan = floorPlan.copy(openings = floorPlan.openings + opening)
        }

        override fun onMoveOpening(id: String, t: Float) {
            floorPlan = floorPlan.copy(openings = floorPlan.openings.map {
                if (it.id == id) it.copy(t = t.coerceIn(0.05f, 0.95f)) else it
            })
        }

        override fun onResizeOpening(id: String, widthCm: Float) {
            floorPlan = floorPlan.copy(openings = floorPlan.openings.map {
                if (it.id == id) it.copy(widthCm = snapOpeningWidth(widthCm, it.type)) else it
            })
        }

        override fun onRemoveOpening(id: String) {
            floorPlan = floorPlan.copy(openings = floorPlan.openings.filter { it.id != id })
            if (selectedOpeningId == id) selectedOpeningId = null
        }

        override fun onSetLeafHidden(hidden: Boolean) = updateOpening { it.copy(leafHidden = hidden) }

        override fun onSetLeafOpen(open: Boolean) = updateOpening { it.copy(leafOpen = open) }

        override fun onRemoveSelectedOpening() {
            val id = selectedOpeningId ?: return
            onRemoveOpening(id)
        }

        private inline fun updateOpening(transform: (WallOpening) -> WallOpening) {
            val id = selectedOpeningId ?: return
            floorPlan = floorPlan.copy(
                openings = floorPlan.openings.map { if (it.id == id) transform(it) else it }
            )
        }

        /** A door prop dropped on a wall becomes a real opening; the prop itself goes away. */
        override fun onDropOpening(nodeA: Int, nodeB: Int, t: Float, widthCm: Float, furnitureId: String) {
            floorPlan = floorPlan.copy(
                openings = floorPlan.openings + WallOpening(
                    id = UUID.randomUUID().toString(),
                    nodeA = nodeA, nodeB = nodeB, level = activeLevel, t = t,
                    type = OpeningType.DOOR,
                    widthCm = widthCm.coerceIn(60f, 200f),
                    style = furnitureId,
                )
            )
            placedFurniture = placedFurniture.filterNot { it.id == furnitureId }
            if (selectedId == furnitureId) selectedId = null
        }

        // ── Stairs ────────────────────────────────────────────────────────────

        override fun onPlaceStair(x: Float, y: Float) {
            val stair = floorPlan.fitStair(
                Stair(id = UUID.randomUUID().toString(), level = activeLevel, x = x, y = y)
            )
            floorPlan = floorPlan.copy(stairs = floorPlan.stairs + stair)
            selectedStairId = stair.id
            placementTool = PlacementTool.NONE
        }

        override fun onMoveStair(id: String, x: Float, y: Float) =
            updateStairById(id) { it.copy(x = x, y = y) }

        override fun onStairWidth(cm: Float) = updateStair { it.copy(widthCm = cm) }

        override fun onStairLength(cm: Float) = updateStair { it.copy(lengthCm = cm) }

        override fun onStairRotate(deg: Float) = updateStair { it.copy(rotationDeg = deg) }

        override fun onStairShape(shape: StairShape) = updateStair { it.copy(shape = shape) }

        override fun onRemoveSelectedStair() {
            val id = selectedStairId ?: return
            floorPlan = floorPlan.copy(stairs = floorPlan.stairs.filter { it.id != id })
            selectedStairId = null
        }

        private inline fun updateStair(transform: (Stair) -> Stair) {
            updateStairById(selectedStairId ?: return, transform)
        }

        private inline fun updateStairById(id: String, transform: (Stair) -> Stair) {
            floorPlan = floorPlan.copy(
                stairs = floorPlan.stairs.map {
                    if (it.id == id) floorPlan.fitStair(transform(it)) else it
                }
            )
        }

        // ── Furniture ─────────────────────────────────────────────────────────

        override fun onMoveFurniture(id: String, x: Float, z: Float) {
            placedFurniture = placedFurniture.map {
                if (it.id == id) it.copy(posX = x, posZ = z) else it
            }
        }


        override fun onAddFurniture(key: String, wallMounted: Boolean) {
            val centerX = floorPlan.nodes.map { it.x }.average().toFloat().takeIf { !it.isNaN() } ?: 190f
            val centerZ = floorPlan.nodes.map { it.y }.average().toFloat().takeIf { !it.isNaN() } ?: 260f
            val (x, z) = findFreeSpot(centerX, centerZ, placedFurniture, floorPlan.nodes)
            val item = catalogItem(key)
            val placed = PlacedFurniture(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                furnitureId = key,
                furnitureName = item?.label ?: key,
                modelUrl = "",
                posX = x, posZ = z,
                isWallMounted = wallMounted,
                level = activeLevel,
                wallMountHeight = item?.wallHeightCm ?: 120f,
            )
            placedFurniture = placedFurniture + placed
            selectedId = placed.id
            showAddFurnitureSheet = false
        }

        override fun onRotate(degrees: Float) = updateSelected { it.copy(rotationY = degrees) }

        override fun onScale(scale: Float) = updateSelected { it.copy(scale = scale) }

        override fun onColorChange(hex: String?) = updateSelected { it.copy(colorOverride = hex) }

        override fun onChangeHeight(heightCm: Float) = updateSelected { it.copy(wallMountHeight = heightCm) }

        override fun onToggleWallMount(mounted: Boolean) = updateSelected { item ->
            if (!mounted) return@updateSelected item.copy(isWallMounted = false)
            val wall = findNearestWall(item.posX, item.posZ, roomPolygons)
            item.copy(
                isWallMounted = true,
                posX = wall?.snappedX ?: item.posX,
                posZ = wall?.snappedZ ?: item.posZ,
            )
        }

        override fun onDeleteSelected() {
            val id = selectedId ?: return
            placedFurniture = placedFurniture.filterNot { it.id == id }
            selectedId = null
        }

        private inline fun updateSelected(transform: (PlacedFurniture) -> PlacedFurniture) {
            val id = selectedId ?: return
            placedFurniture = placedFurniture.map { if (it.id == id) transform(it) else it }
        }

        // ── Surfaces ──────────────────────────────────────────────────────────

        override fun onWallPreset(index: Int) {
            floorPlan = floorPlan.withSurface(activeLevel) {
                it.copy(wallPresetIdx = index, wallColor = "")
            }
        }

        override fun onFloorPreset(index: Int) {
            floorPlan = floorPlan.withSurface(activeLevel) { it.copy(floorPresetIdx = index) }
        }

        override fun onStairPreset(index: Int) {
            stairPresetIdx = index
            persistSurfaces()
        }

        override fun onWallColor(hex: String?) {
            floorPlan = floorPlan.withSurface(activeLevel) { it.copy(wallColor = hex.orEmpty()) }
        }

        override fun onApplyPalette(palette: ColorPalette) {
            floorPlan = floorPlan.withSurface(activeLevel) { it.copy(wallColor = palette.background) }
            notify(app.getString(R.string.palette_applied, palette.name))
        }

        override fun onRoomHeight(cm: Float) {
            roomHeightCm = cm
            viewModelScope.launch {
                db.roomDao().getRoomById(roomId)?.let {
                    db.roomDao().updateRoom(it.copy(heightCm = cm, updatedAt = System.currentTimeMillis()))
                }
            }
        }

        override fun onShadows(enabled: Boolean) {
            shadowsOn = enabled
            persistSurfaces()
        }

        override fun onAutoHideWalls(enabled: Boolean) {
            autoHideWalls = enabled
            persistSurfaces()
        }
    }

    init {
        load()
        observeFurnitureForAutoSave()
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    private fun load() {
        screenState.loading = true
        viewModelScope.launch {
            db.roomDao().getRoomById(roomId)?.let { room ->
                if (room.floorPlanJson.isNotBlank()) {
                    screenState.floorPlan = Json.decodeFromString(room.floorPlanJson)
                    screenState.drawingPhase = DrawingPhase.EDITING
                    screenState.editorMode = EditorMode.DRAW_WALLS
                }
                screenState.roomHeightCm = room.heightCm
                screenState.stairPresetIdx = room.stairPresetIdx
                screenState.shadowsOn = room.shadowsEnabled
                screenState.autoHideWalls = room.autoHideWalls
            }
            // Items whose pack was removed would render as nothing — drop them on load.
            screenState.placedFurniture = db.placedFurnitureDao()
                .getFurnitureForRoom(roomId).first()
                .filter { catalogItem(it.furnitureId) != null }
            screenState.loading = false
            loaded = true
        }
    }

    private fun observeFurnitureForAutoSave() {
        viewModelScope.launch {
            snapshotFlow { screenState.placedFurniture }.collectLatest { items ->
                if (!loaded) return@collectLatest
                delay(AUTO_SAVE_DELAY_MS)
                persistFurniture(items)
            }
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private suspend fun persistPlan() {
        val json = Json.encodeToString(screenState.floorPlan)
        db.roomDao().getRoomById(roomId)?.let { existing ->
            db.roomDao().updateRoom(
                existing.copy(floorPlanJson = json, updatedAt = System.currentTimeMillis())
            )
        }
    }

    private suspend fun persistFurniture(items: List<PlacedFurniture>) {
        db.placedFurnitureDao().clearRoomFurniture(roomId)
        items.forEach { db.placedFurnitureDao().insertPlacedFurniture(it.copy(roomId = roomId)) }
    }

    private fun persistSurfaces() {
        val state: DesignerState = screenState
        viewModelScope.launch {
            db.roomDao().getRoomById(roomId)?.let { existing ->
                db.roomDao().updateRoom(
                    existing.copy(
                        stairPresetIdx = state.stairPresetIdx,
                        shadowsEnabled = state.shadowsOn,
                        autoHideWalls = state.autoHideWalls,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    // ── Undo ──────────────────────────────────────────────────────────────────

    private fun undoLastStep() = with(screenState) {
        when (drawingPhase) {
            DrawingPhase.CLOSED -> {
                val lastRoom = floorPlan.rooms.last()
                floorPlan = floorPlan.dropLastRoom()
                currentPath = lastRoom
                drawingPhase = DrawingPhase.PLACING
            }
            DrawingPhase.PLACING -> when {
                currentPath.size > 1 -> {
                    val lastIdx = currentPath.last()
                    if (isNodeDisposable(lastIdx, dropLast = true)) {
                        floorPlan = floorPlan.copy(nodes = floorPlan.nodes.dropLast(1))
                    }
                    currentPath = currentPath.dropLast(1)
                }
                currentPath.size == 1 -> {
                    if (isNodeDisposable(currentPath.first(), dropLast = false)) {
                        floorPlan = floorPlan.copy(nodes = floorPlan.nodes.dropLast(1))
                    }
                    currentPath = emptyList()
                    drawingPhase = if (hasRooms) DrawingPhase.EDITING else DrawingPhase.PLACING
                }
                else -> Unit
            }
            DrawingPhase.EDITING -> Unit
        }
    }

    /** A node can be dropped only if it is the newest one and no committed room or earlier path step uses it. */
    private fun DesignerState.isNodeDisposable(index: Int, dropLast: Boolean): Boolean {
        val usedElsewhere = floorPlan.rooms.any { index in it } ||
            (dropLast && currentPath.dropLast(1).contains(index))
        return !usedElsewhere && index == floorPlan.nodes.lastIndex
    }

    /** Openings land on 5 cm steps inside the range real joinery comes in. */
    private fun snapOpeningWidth(raw: Float, type: OpeningType): Float {
        val range = if (type == OpeningType.DOOR) 60f..200f else 40f..300f
        return (kotlin.math.round(raw / 5f) * 5f).coerceIn(range)
    }

    companion object {
        private const val DEFAULT_DOOR_CM = 90f
        private const val DEFAULT_WINDOW_CM = 120f
    }
}

/** Nearest spot to (cx,cz) not within 60 cm of existing furniture, searched in rings and kept inside the plan bbox. */
private fun findFreeSpot(
    cx: Float,
    cz: Float,
    existing: List<PlacedFurniture>,
    nodes: List<WallPoint>,
): Pair<Float, Float> {
    val minX = nodes.minOfOrNull { it.x } ?: (cx - 200f)
    val maxX = nodes.maxOfOrNull { it.x } ?: (cx + 200f)
    val minZ = nodes.minOfOrNull { it.y } ?: (cz - 200f)
    val maxZ = nodes.maxOfOrNull { it.y } ?: (cz + 200f)
    fun free(x: Float, z: Float) = existing.none { hypot(it.posX - x, it.posZ - z) < 60f }
    if (free(cx, cz)) return cx to cz
    for (ring in 1..8) {
        val radius = ring * 70f
        val steps = ring * 8
        for (i in 0 until steps) {
            val angle = 2.0 * Math.PI * i / steps
            val x = (cx + radius * cos(angle)).toFloat().coerceIn(minX + 30f, maxX - 30f)
            val z = (cz + radius * sin(angle)).toFloat().coerceIn(minZ + 30f, maxZ - 30f)
            if (free(x, z)) return x to z
        }
    }
    return cx to cz
}
