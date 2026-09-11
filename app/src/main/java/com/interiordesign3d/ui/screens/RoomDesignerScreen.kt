package com.interiordesign3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import com.interiordesign3d.data.repository.AppDatabase
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// ─── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDesignerScreen(
    roomId: String,
    onNavigateToColorPicker: () -> Unit,
    onBack: () -> Unit
) {
    val context     = LocalContext.current
    val db          = remember { AppDatabase.getInstance(context) }
    val scope       = rememberCoroutineScope()
    val roomHeight  = 260f

    // ── Floor plan state (start empty; LaunchedEffect loads from DB) ──────────
    var floorPlan    by remember { mutableStateOf(FloorPlan()) }
    var drawingPhase by remember { mutableStateOf(DrawingPhase.PLACING) }
    var currentPath  by remember { mutableStateOf(emptyList<Int>()) }

    // ── Design mode state ─────────────────────────────────────────────────────
    var placedFurniture    by remember { mutableStateOf(mutableListOf<PlacedFurniture>()) }
    var selectedId         by remember { mutableStateOf<String?>(null) }
    var showAddFurnitureSheet by remember { mutableStateOf(false) }
    var editorMode     by remember { mutableStateOf(EditorMode.DRAW_WALLS) }
    var viewMode       by remember { mutableStateOf(ViewMode.PERSPECTIVE) }
    var snapEnabled    by remember { mutableStateOf(true) }
    var saveSuccess    by remember { mutableStateOf(false) }
    var placementTool  by remember { mutableStateOf(PlacementTool.NONE) }
    var wallColorHex   by remember { mutableStateOf("#F5F0EB") }
    var use3DEngine    by remember { mutableStateOf(true) }
    var wallPresetIdx  by remember { mutableStateOf(0) }
    var floorPresetIdx by remember { mutableStateOf(0) }
    var shadowsOn      by remember { mutableStateOf(false) }
    var autoHideWalls  by remember { mutableStateOf(false) }   // mặc định giữ nguyên 4 tường; bật tay trong SurfaceSheet
    var showSurfaceSheet by remember { mutableStateOf(false) }
    var loaded         by remember { mutableStateOf(false) }

    // ── Load from DB on open ──────────────────────────────────────────────────
    LaunchedEffect(roomId) {
        val room = db.roomDao().getRoomById(roomId)
        if (room != null && room.floorPlanJson.isNotBlank()) {
            floorPlan    = Json.decodeFromString(room.floorPlanJson)
            drawingPhase = DrawingPhase.EDITING
            editorMode   = EditorMode.DRAW_WALLS
        }
        if (room != null) wallColorHex = room.wallColor
        val items = db.placedFurnitureDao().getFurnitureForRoom(roomId).first()
            .filter { catalogItem(it.furnitureId) != null }  // drop items from removed packs
        placedFurniture = items.toMutableList()
        loaded = true
    }

    // ── Auto-save placed furniture (debounced) so changes persist without a manual Save ──
    LaunchedEffect(placedFurniture, loaded) {
        if (!loaded) return@LaunchedEffect
        kotlinx.coroutines.delay(400)
        val snapshot = placedFurniture.toList()
        db.placedFurnitureDao().clearRoomFurniture(roomId)
        snapshot.forEach { db.placedFurnitureDao().insertPlacedFurniture(it.copy(roomId = roomId)) }
    }

    // ── Save toast auto-hide ──────────────────────────────────────────────────
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(1500)
            saveSuccess = false
        }
    }

    // ── Derived ───────────────────────────────────────────────────────────────
    val hasRooms = floorPlan.rooms.isNotEmpty()
    val roomPolygons = remember(floorPlan) {
        floorPlan.rooms.map { room -> room.map { floorPlan.nodes[it] } }
    }

    Scaffold(
        topBar = {
            DesignerTopBar(
                editorMode = editorMode,
                drawingPhase = drawingPhase,
                viewMode = viewMode,
                onViewChange = { viewMode = it },
                showViewModes = !use3DEngine,
                onEditFloorPlan = {
                    scope.launch {
                        db.placedFurnitureDao().clearRoomFurniture(roomId)
                        placedFurniture.forEach { item ->
                            db.placedFurnitureDao().insertPlacedFurniture(item.copy(roomId = roomId))
                        }
                        editorMode = EditorMode.DRAW_WALLS
                    }
                },
                onColors = onNavigateToColorPicker,
                onSave = {
                    scope.launch {
                        val json = Json.encodeToString(floorPlan)
                        db.roomDao().getRoomById(roomId)?.let { existing ->
                            db.roomDao().updateRoom(
                                existing.copy(
                                    floorPlanJson = json,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                        db.placedFurnitureDao().clearRoomFurniture(roomId)
                        placedFurniture.forEach { item ->
                            db.placedFurnitureDao().insertPlacedFurniture(item.copy(roomId = roomId))
                        }
                        saveSuccess = true
                    }
                },
                onBack = onBack
            )
        },
        floatingActionButton = {
            when (editorMode) {
                EditorMode.DRAW_WALLS -> {
                    AnimatedVisibility(
                        visible = hasRooms,
                        enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (drawingPhase == DrawingPhase.EDITING) {
                                SmallFloatingActionButton(
                                    onClick = { showAddFurnitureSheet = true },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) { Icon(Icons.Filled.AddCircle, "Add Furniture") }
                            }
                            ExtendedFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        val json = Json.encodeToString(floorPlan)
                                        db.roomDao().getRoomById(roomId)?.let { existing ->
                                            db.roomDao().updateRoom(existing.copy(
                                                floorPlanJson = json,
                                                updatedAt = System.currentTimeMillis()
                                            ))
                                        }
                                        editorMode = EditorMode.DESIGN
                                    }
                                },
                                icon = { Icon(Icons.Filled.Chair, null) },
                                text = { Text("Design Room") },
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                EditorMode.DESIGN -> {
                    FloatingActionButton(
                        onClick = { showAddFurnitureSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) { Icon(Icons.Filled.AddCircle, "Add Furniture") }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (editorMode) {
                EditorMode.DRAW_WALLS -> {
                    WallDrawingCanvas(
                        floorPlan = floorPlan,
                        drawingPhase = drawingPhase,
                        currentPath = currentPath,
                        gridSizeCm = if (snapEnabled) 10f else 0f,
                        placementTool = placementTool,
                        onPlaceOpening = { rIdx, eIdx, t, type ->
                            val op = WallOpening(
                                id = UUID.randomUUID().toString(),
                                roomIdx = rIdx, edgeIdx = eIdx, t = t, type = type,
                                widthCm = if (type == OpeningType.DOOR) 90f else 100f
                            )
                            floorPlan = floorPlan.copy(openings = floorPlan.openings + op)
                        },
                        onMoveOpening = { id, newT ->
                            floorPlan = floorPlan.copy(openings = floorPlan.openings.map {
                                if (it.id == id) it.copy(t = newT.coerceIn(0.05f, 0.95f)) else it
                            })
                        },
                        onResizeOpening = { id, newWidthCm ->
                            floorPlan = floorPlan.copy(openings = floorPlan.openings.map {
                                if (it.id == id) it.copy(widthCm = newWidthCm.coerceIn(40f, 300f)) else it
                            })
                        },
                        onRemoveOpening = { id ->
                            floorPlan = floorPlan.copy(openings = floorPlan.openings.filter { it.id != id })
                        },
                        placedFurniture = placedFurniture,
                        onMoveFurnitureInPlan = { id, x, z ->
                            placedFurniture = placedFurniture.map {
                                if (it.id == id) it.copy(posX = x, posZ = z) else it
                            }.toMutableList()
                        },
                        onTapFurniture = { id -> selectedId = id },
                        onAddNewPoint = { pt ->
                            val idx = floorPlan.nodes.size
                            floorPlan = floorPlan.copy(nodes = floorPlan.nodes + pt)
                            currentPath = currentPath + idx
                        },
                        onSnapToNode = { idx ->
                            currentPath = currentPath + idx
                        },
                        onClosePath = {
                            if (currentPath.size >= 3) {
                                floorPlan = floorPlan.copy(rooms = floorPlan.rooms + listOf(currentPath))
                                drawingPhase = DrawingPhase.CLOSED
                            }
                        },
                        onMoveNode = { idx, pt ->
                            floorPlan = floorPlan.copy(
                                nodes = floorPlan.nodes.toMutableList().also { it[idx] = pt }
                            )
                        },
                        onStartFromNode = { idx ->
                            currentPath = listOf(idx)
                            drawingPhase = DrawingPhase.PLACING
                        },
                        onStartNewPoint = { pt ->
                            val idx = floorPlan.nodes.size
                            floorPlan = floorPlan.copy(nodes = floorPlan.nodes + pt)
                            currentPath = listOf(idx)
                            drawingPhase = DrawingPhase.PLACING
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FloorPlanToolbar(
                        phase = drawingPhase,
                        currentPath = currentPath,
                        hasRooms = hasRooms,
                        snapEnabled = snapEnabled,
                        onDone = {
                            currentPath = emptyList()
                            drawingPhase = DrawingPhase.EDITING
                        },
                        onUndo = {
                            when (drawingPhase) {
                                DrawingPhase.CLOSED -> {
                                    // Undo close: pop the last committed room, reopen path
                                    val lastRoom = floorPlan.rooms.last()
                                    floorPlan = floorPlan.copy(rooms = floorPlan.rooms.dropLast(1))
                                    currentPath = lastRoom
                                    drawingPhase = DrawingPhase.PLACING
                                }
                                DrawingPhase.PLACING -> {
                                    if (currentPath.size > 1) {
                                        val lastIdx = currentPath.last()
                                        val usedElsewhere = floorPlan.rooms.any { lastIdx in it } ||
                                            currentPath.dropLast(1).contains(lastIdx)
                                        if (!usedElsewhere && lastIdx == floorPlan.nodes.lastIndex) {
                                            floorPlan = floorPlan.copy(nodes = floorPlan.nodes.dropLast(1))
                                        }
                                        currentPath = currentPath.dropLast(1)
                                    } else if (currentPath.size == 1) {
                                        // Cancel current path entirely
                                        val startIdx = currentPath.first()
                                        val usedElsewhere = floorPlan.rooms.any { startIdx in it }
                                        if (!usedElsewhere && startIdx == floorPlan.nodes.lastIndex) {
                                            floorPlan = floorPlan.copy(nodes = floorPlan.nodes.dropLast(1))
                                        }
                                        currentPath = emptyList()
                                        drawingPhase = if (hasRooms) DrawingPhase.EDITING else DrawingPhase.PLACING
                                    }
                                }
                                DrawingPhase.EDITING -> Unit
                            }
                        },
                        onClear = {
                            floorPlan = FloorPlan()
                            currentPath = emptyList()
                            drawingPhase = DrawingPhase.PLACING
                        },
                        onToggleSnap = { snapEnabled = !snapEnabled },
                        placementTool = placementTool,
                        onToolChange = { placementTool = it }
                    )
                }

                EditorMode.DESIGN -> {
                    BoxWithConstraints(Modifier.weight(1f)) {
                    val halfH = maxHeight * 0.5f
                    Box(Modifier.fillMaxSize()) {
                    if (use3DEngine) {
                        FilamentRoomViewport(
                            floorPlan = floorPlan,
                            roomPolygons = roomPolygons,
                            placedFurniture = placedFurniture,
                            roomHeight = roomHeight,
                            wallModel = WALL_PRESETS[wallPresetIdx].model,
                            wallColorHex = WALL_PRESETS[wallPresetIdx].colorHex,
                            wallTileM = WALL_PRESETS[wallPresetIdx].tileM,
                            floorModel = FLOOR_PRESETS[floorPresetIdx].model,
                            floorColorHex = FLOOR_PRESETS[floorPresetIdx].colorHex,
                            floorTileM = FLOOR_PRESETS[floorPresetIdx].tileM,
                            shadows = shadowsOn,
                            autoHideWalls = autoHideWalls,
                            onDropOpening = { rIdx, eIdx, t, widthCm, furnitureId ->
                                // door prop → real wall opening (cuts the wall), prop removed
                                floorPlan = floorPlan.copy(openings = floorPlan.openings + WallOpening(
                                    id = UUID.randomUUID().toString(), roomIdx = rIdx, edgeIdx = eIdx,
                                    t = t, type = OpeningType.DOOR, widthCm = widthCm.coerceIn(60f, 200f),
                                    style = furnitureId))
                                placedFurniture = placedFurniture.filter { it.id != furnitureId }.toMutableList()
                                if (selectedId == furnitureId) selectedId = null
                            },
                            onSelectFurniture = { selectedId = it },
                            onMoveFurniture = { id, x, z ->
                                placedFurniture = placedFurniture.map {
                                    if (it.id == id) it.copy(posX = x, posZ = z) else it
                                }.toMutableList()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                    RoomViewport3D(
                        floorPlan = floorPlan,
                        roomHeight = roomHeight,
                        placedFurniture = placedFurniture,
                        selectedId = selectedId,
                        viewMode = viewMode,
                        roomPolygons = roomPolygons,
                        onSelectFurniture = { selectedId = it },
                        onMoveFurniture = { id, x, z ->
                            placedFurniture = placedFurniture.map {
                                if (it.id == id) it.copy(posX = x, posZ = z) else it
                            }.toMutableList()
                        },
                        onMoveWallFurniture = { id, x, z, h ->
                            placedFurniture = placedFurniture.map {
                                if (it.id == id) it.copy(posX = x, posZ = z, wallMountHeight = h) else it
                            }.toMutableList()
                        },
                        wallColorHex = wallColorHex,
                        gridMinorCm = if (snapEnabled) 10f else 50f,
                        onWallColorChange = { hex ->
                            wallColorHex = hex
                            scope.launch {
                                db.roomDao().getRoomById(roomId)?.let {
                                    db.roomDao().updateRoom(
                                        it.copy(wallColor = hex, updatedAt = System.currentTimeMillis())
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    } // else (Canvas viewport)

                    // Toggle 3D engine ↔ Canvas + wall/floor surface picker
                    Column(Modifier.align(Alignment.TopStart).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallFloatingActionButton(
                            onClick = { use3DEngine = !use3DEngine },
                            containerColor = if (use3DEngine) MaterialTheme.colorScheme.primary
                                             else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Icon(Icons.Filled.ViewInAr,
                                if (use3DEngine) "Using 3D engine" else "Using canvas",
                                Modifier.size(18.dp),
                                tint = if (use3DEngine) MaterialTheme.colorScheme.onPrimary
                                       else LocalContentColor.current)
                        }
                        if (use3DEngine) SmallFloatingActionButton(
                            onClick = { showSurfaceSheet = true },
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) { Icon(Icons.Filled.Wallpaper, "Tường & sàn", Modifier.size(18.dp)) }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedId != null,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically { it }, exit = slideOutVertically { it }
                    ) {
                    Box(Modifier.height(halfH).fillMaxWidth()) {
                        placedFurniture.find { it.id == selectedId }?.let { sel ->
                            FurnitureControlPanel(
                                item = sel,
                                onRotate = { deg ->
                                    placedFurniture = placedFurniture.map {
                                        if (it.id == selectedId) it.copy(rotationY = deg) else it
                                    }.toMutableList()
                                },
                                onScale = { s ->
                                    placedFurniture = placedFurniture.map {
                                        if (it.id == selectedId) it.copy(scale = s) else it
                                    }.toMutableList()
                                },
                                onDelete = {
                                    placedFurniture.removeIf { it.id == selectedId }
                                    placedFurniture = placedFurniture.toMutableList()
                                    selectedId = null
                                },
                                onDeselect = { selectedId = null },
                                onToggleWallMount = { mounted ->
                                    placedFurniture = placedFurniture.map {
                                        if (it.id == selectedId) {
                                            if (mounted) {
                                                val wall = findNearestWall(it.posX, it.posZ, roomPolygons)
                                                it.copy(isWallMounted = true,
                                                        posX = wall?.snappedX ?: it.posX,
                                                        posZ = wall?.snappedZ ?: it.posZ)
                                            } else it.copy(isWallMounted = false)
                                        } else it
                                    }.toMutableList()
                                },
                                onChangeHeight = { h ->
                                    placedFurniture = placedFurniture.map {
                                        if (it.id == selectedId) it.copy(wallMountHeight = h) else it
                                    }.toMutableList()
                                },
                                onColorChange = { hex ->
                                    placedFurniture = placedFurniture.map {
                                        if (it.id == selectedId) it.copy(colorOverride = hex) else it
                                    }.toMutableList()
                                }
                            )
                        }
                    } // Box panel
                    } // AnimatedVisibility
                    } // Box overlay
                    } // BoxWithConstraints
                }
            }
        }
    }

    if (showAddFurnitureSheet) {
        AddFurnitureSheet(
            onAdd = { key, wallMounted ->
                val cx = floorPlan.nodes.map { it.x }.average().toFloat().takeIf { !it.isNaN() } ?: 190f
                val cz = floorPlan.nodes.map { it.y }.average().toFloat().takeIf { !it.isNaN() } ?: 260f
                val (sx, sz) = findFreeSpot(cx, cz, placedFurniture, floorPlan.nodes)
                val item = catalogItem(key)
                val placed = PlacedFurniture(
                    id = UUID.randomUUID().toString(), roomId = roomId,
                    furnitureId = key, furnitureName = item?.label ?: key,
                    modelUrl = "",
                    posX = sx, posZ = sz, isWallMounted = wallMounted,
                    wallMountHeight = item?.wallHeightCm ?: 120f
                )
                placedFurniture = (placedFurniture + placed).toMutableList()
                selectedId = placed.id
                showAddFurnitureSheet = false
            },
            onDismiss = { showAddFurnitureSheet = false }
        )
    }

    if (showSurfaceSheet) {
        SurfaceSheet(
            wallIdx = wallPresetIdx, floorIdx = floorPresetIdx, shadows = shadowsOn, autoHide = autoHideWalls,
            onWall = { wallPresetIdx = it }, onFloor = { floorPresetIdx = it },
            onShadows = { shadowsOn = it }, onAutoHide = { autoHideWalls = it },
            onDismiss = { showSurfaceSheet = false }
        )
    }

    // ── Save success toast ────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = saveSuccess,
        enter = fadeIn() + slideInVertically { -it },
        exit  = fadeOut() + slideOutVertically { -it },
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
            .padding(top = 80.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 6.dp
        ) {
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Check, null, Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text("Saved", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesignerTopBar(
    editorMode: EditorMode,
    drawingPhase: DrawingPhase,
    viewMode: ViewMode,
    onViewChange: (ViewMode) -> Unit,
    showViewModes: Boolean,               // Canvas-only view modes; hidden for the 3D engine
    onEditFloorPlan: () -> Unit,
    onColors: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val subtitle = when {
        editorMode == EditorMode.DESIGN -> "Place Furniture"
        drawingPhase == DrawingPhase.PLACING -> "Draw walls — tap to place corners"
        drawingPhase == DrawingPhase.CLOSED  -> "Room closed — tap Done to confirm"
        else                                 -> "Editing — tap corner to add room"
    }
    TopAppBar(
        title = {
            Column {
                Text("Floor Plan", style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
        actions = {
            if (editorMode == EditorMode.DESIGN) {
                if (showViewModes) ViewMode.entries.forEach { mode ->
                    IconButton(onClick = { onViewChange(mode) }) {
                        Icon(mode.icon, mode.label,
                            tint = if (viewMode == mode) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onEditFloorPlan) { Icon(Icons.Filled.EditNote, "Edit Floor Plan") }
                IconButton(onClick = onColors) { Icon(Icons.Filled.Palette, "Colors") }
                IconButton(onClick = onSave)   { Icon(Icons.Filled.Save,    "Save")   }
            }
        }
    )
}

// ─── Spawn helper ─────────────────────────────────────────────────────────────

/** Nearest spot to (cx,cz) not within 60 cm of existing furniture, searched in rings and kept inside the plan bbox. */
private fun findFreeSpot(
    cx: Float, cz: Float, existing: List<PlacedFurniture>, nodes: List<WallPoint>,
): Pair<Float, Float> {
    val minX = nodes.minOfOrNull { it.x } ?: (cx - 200f); val maxX = nodes.maxOfOrNull { it.x } ?: (cx + 200f)
    val minZ = nodes.minOfOrNull { it.y } ?: (cz - 200f); val maxZ = nodes.maxOfOrNull { it.y } ?: (cz + 200f)
    fun free(x: Float, z: Float) = existing.none { kotlin.math.hypot(it.posX - x, it.posZ - z) < 60f }
    if (free(cx, cz)) return cx to cz
    val step = 70f
    for (ring in 1..8) {
        val r = ring * step
        val n = ring * 8
        for (i in 0 until n) {
            val a = 2.0 * Math.PI * i / n
            val x = (cx + r * kotlin.math.cos(a)).toFloat().coerceIn(minX + 30f, maxX - 30f)
            val z = (cz + r * kotlin.math.sin(a)).toFloat().coerceIn(minZ + 30f, maxZ - 30f)
            if (free(x, z)) return x to z
        }
    }
    return cx to cz
}
