package com.interiordesign3d.ui.screen.designer.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState
import com.interiordesign3d.data.catalog.FLOOR_PRESETS
import com.interiordesign3d.data.catalog.GROUND_PRESETS
import com.interiordesign3d.data.catalog.ROOF_PRESETS
import com.interiordesign3d.data.catalog.WALL_PRESETS
import com.interiordesign3d.data.models.Balcony
import com.interiordesign3d.data.models.ColorPalette
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.OpeningType
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.models.RoofShape
import com.interiordesign3d.data.models.Stair
import com.interiordesign3d.data.models.StairShape
import com.interiordesign3d.data.models.WallOpening
import com.interiordesign3d.data.models.WallPoint
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.EditorMode
import com.interiordesign3d.ui.screen.designer.PlacementTool

@Stable
open class DesignerState : BaseScreenState() {

    // ── Plan ──────────────────────────────────────────────────────────────────
    var floorPlan by mutableStateOf(FloorPlan())
    var drawingPhase by mutableStateOf(DrawingPhase.PLACING)
    var currentPath by mutableStateOf(emptyList<Int>())
    var snapEnabled by mutableStateOf(true)
    var placementTool by mutableStateOf(PlacementTool.NONE)

    // ── Furniture ─────────────────────────────────────────────────────────────
    var placedFurniture by mutableStateOf(emptyList<PlacedFurniture>())
    var selectedId by mutableStateOf<String?>(null)
    var selectedOpeningId by mutableStateOf<String?>(null)
    var selectedStairId by mutableStateOf<String?>(null)
    var selectedBalconyId by mutableStateOf<String?>(null)

    var editorMode by mutableStateOf(EditorMode.DRAW_WALLS)
    var activeLevel by mutableStateOf(0)

    // ── Surfaces ──────────────────────────────────────────────────────────────
    var stairPresetIdx by mutableStateOf(2)

    // Wall and floor finishes belong to the storey, so they are read straight off the plan.
    val wallPresetIdx: Int get() = floorPlan.surfaceOf(activeLevel).wallPresetIdx
    val floorPresetIdx: Int get() = floorPlan.surfaceOf(activeLevel).floorPresetIdx
    val wallColorOverride: String? get() = floorPlan.surfaceOf(activeLevel).wallColor.ifBlank { null }
    var shadowsOn by mutableStateOf(false)
    var autoHideWalls by mutableStateOf(false)

    // ── Sheets ────────────────────────────────────────────────────────────────
    var showAddFurnitureSheet by mutableStateOf(false)
    var showSurfaceSheet by mutableStateOf(false)

    var roomHeightCm by mutableStateOf(DEFAULT_ROOM_HEIGHT_CM)

    val hasRooms: Boolean by derivedStateOf { floorPlan.rooms.isNotEmpty() }

    /** Storeys that exist, plus the empty one being started. */
    val levelCount: Int by derivedStateOf { maxOf(floorPlan.levelCount, activeLevel + 1) }

    val roomPolygons: List<List<WallPoint>> by derivedStateOf {
        floorPlan.rooms.map { room -> room.map { floorPlan.nodes[it] } }
    }

    /** Only the storey being edited: hit-testing and wall snapping must ignore the others. */
    val activeRoomPolygons: List<List<WallPoint>> by derivedStateOf {
        floorPlan.roomsOnLevel(activeLevel).map { i -> floorPlan.rooms[i].map { floorPlan.nodes[it] } }
    }

    val selectedItem: PlacedFurniture? by derivedStateOf {
        placedFurniture.firstOrNull { it.id == selectedId }
    }

    val selectedOpening: WallOpening? by derivedStateOf {
        floorPlan.openings.firstOrNull { it.id == selectedOpeningId }
    }

    val selectedBalcony: Balcony?
        get() = floorPlan.balconies.firstOrNull { it.id == selectedBalconyId }

    val selectedStair: Stair? by derivedStateOf {
        floorPlan.stairs.firstOrNull { it.id == selectedStairId }
    }

    val wallPreset get() = WALL_PRESETS[wallPresetIdx.coerceIn(WALL_PRESETS.indices)]
    val floorPreset get() = FLOOR_PRESETS[floorPresetIdx.coerceIn(FLOOR_PRESETS.indices)]
    val stairPreset get() = FLOOR_PRESETS[stairPresetIdx.coerceIn(FLOOR_PRESETS.indices)]
    val exteriorSurface get() = floorPlan.exterior
    // Rectangles the roof splits into, and whether a pitched roof is possible at all. Both walk the
    // whole outline, so they are cached: as plain getters they re-ran the decomposition on every
    // recomposition of the panel that shows them.
    val roofMassCount: Int by derivedStateOf { floorPlan.roofMasses(topLevel).size }
    val roofCanPitch: Boolean by derivedStateOf { floorPlan.isOrthogonal(topLevel) }
    val topLevel: Int get() = (floorPlan.levelCount - 1).coerceAtLeast(0)
    val roofPresetIdx: Int get() = floorPlan.exterior.roofPresetIdx
    val groundPresetIdx: Int get() = floorPlan.exterior.groundPresetIdx
    val roofPreset get() = ROOF_PRESETS[roofPresetIdx.coerceIn(ROOF_PRESETS.indices)]
    val groundPreset get() = GROUND_PRESETS[groundPresetIdx.coerceIn(GROUND_PRESETS.indices)]
    val wallColorHex get() = wallColorOverride ?: wallPreset.colorHex

    // ── Navigation / persistence ──────────────────────────────────────────────
    open fun onBack() {}
    open fun onSave() {}
    open fun onEditFloorPlan() {}
    open fun onEnterDesign() {}
    open fun onToggleExterior() {}
    open fun onSelectLevel(level: Int) {
        activeLevel = level
        selectedId = null
        selectedOpeningId = null
        selectedStairId = null
        currentPath = emptyList()
    }
    open fun onAddLevel() { onSelectLevel(levelCount) }

    // ── Wall drawing ──────────────────────────────────────────────────────────
    open fun onAddNewPoint(point: WallPoint) {}
    open fun onSnapToNode(index: Int) {}
    open fun onClosePath() {}
    open fun onMoveNode(index: Int, point: WallPoint) {}
    open fun onStartFromNode(index: Int) {}
    open fun onStartNewPoint(point: WallPoint) {}
    open fun onDone() {}
    open fun onUndo() {}
    open fun onClear() {}
    open fun onToggleSnap() { snapEnabled = !snapEnabled }
    open fun onToolChange(tool: PlacementTool) { placementTool = tool }

    // ── Openings ──────────────────────────────────────────────────────────────
    open fun onPlaceOpening(roomIdx: Int, edgeIdx: Int, t: Float, type: OpeningType) {}
    open fun onMoveOpening(id: String, t: Float) {}
    open fun onResizeOpening(id: String, widthCm: Float) {}
    open fun onRemoveOpening(id: String) {}
    open fun onSelectOpening(id: String?) { selectedOpeningId = id; if (id != null) selectedId = null }
    open fun onSetLeafHidden(hidden: Boolean) {}
    open fun onSetLeafOpen(open: Boolean) {}
    open fun onRemoveSelectedOpening() {}
    open fun onPlaceStair(x: Float, y: Float) {}
    open fun onMoveStair(id: String, x: Float, y: Float) {}
    open fun onSelectStair(id: String?) { selectedStairId = id; if (id != null) { selectedId = null; selectedOpeningId = null } }
    open fun onStairWidth(cm: Float) {}
    open fun onStairLength(cm: Float) {}
    open fun onStairRotate(deg: Float) {}
    open fun onStairLeg(cm: Float) {}
    open fun onStairWell(cm: Float) {}
    open fun onStairShape(shape: StairShape) {}
    open fun onPlaceBalcony(nodeA: Int, nodeB: Int, t: Float) {}
    open fun onMoveBalcony(id: String, t: Float) {}
    open fun onSelectBalcony(id: String?) { selectedBalconyId = id }
    open fun onBalconyWidth(cm: Float) {}
    open fun onBalconyDepth(cm: Float) {}
    open fun onRemoveSelectedBalcony() {}
    open fun onRemoveSelectedStair() {}
    open fun onDropOpening(nodeA: Int, nodeB: Int, t: Float, widthCm: Float, furnitureId: String) {}

    // ── Furniture ─────────────────────────────────────────────────────────────
    open fun onSelectFurniture(id: String?) { selectedId = id; if (id != null) selectedOpeningId = null }
    open fun onMoveFurniture(id: String, x: Float, z: Float) {}
    open fun onAddFurniture(key: String, wallMounted: Boolean) {}
    open fun onRotate(degrees: Float) {}
    open fun onScale(scale: Float) {}
    open fun onDeleteSelected() {}
    open fun onDeselect() { selectedId = null }
    open fun onToggleWallMount(mounted: Boolean) {}
    open fun onChangeHeight(heightCm: Float) {}
    open fun onColorChange(hex: String?) {}

    // ── Surfaces ──────────────────────────────────────────────────────────────
    open fun onShowAddFurniture() { showAddFurnitureSheet = true }
    open fun onDismissAddFurniture() { showAddFurnitureSheet = false }
    open fun onShowSurfaceSheet() { showSurfaceSheet = true }
    open fun onDismissSurfaceSheet() { showSurfaceSheet = false }
    open fun onWallPreset(index: Int) {}
    open fun onFloorPreset(index: Int) {}
    open fun onStairPreset(index: Int) {}
    open fun onRoofShape(shape: RoofShape) {}
    open fun onRoofPitch(deg: Float) {}
    open fun onRoofEaves(cm: Float) {}
    open fun onRoofHipFactor(pct: Float) {}
    open fun onRoofPreset(index: Int) {}
    open fun onGroundPreset(index: Int) {}
    open fun onWallColor(hex: String?) {}
    open fun onApplyPalette(palette: ColorPalette) {}
    open fun onRoomHeight(cm: Float) {}
    open fun onShadows(enabled: Boolean) {}
    open fun onAutoHideWalls(enabled: Boolean) {}
}

const val DEFAULT_ROOM_HEIGHT_CM = 260f
