package com.interiordesign3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import kotlin.math.*

// ─── 3D Viewport ─────────────────────────────────────────────────────────────

@Composable
fun RoomViewport3D(
    floorPlan: FloorPlan,
    roomHeight: Float,
    placedFurniture: List<PlacedFurniture>,
    selectedId: String?,
    viewMode: ViewMode,
    roomPolygons: List<List<WallPoint>>,
    onSelectFurniture: (String?) -> Unit,
    onEditFurniture: (String) -> Unit = {},
    onMoveFurniture: (String, Float, Float) -> Unit,
    onMoveWallFurniture: (String, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var cameraAzimuth   by remember { mutableStateOf(35f) }
    var cameraElevation by remember { mutableStateOf(50f) }
    var zoom            by remember { mutableStateOf(1f) }
    var draggingId      by remember { mutableStateOf<String?>(null) }
    var canvasSize      by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(viewMode) {
        when (viewMode) {
            ViewMode.TOP_DOWN  -> { cameraAzimuth = 0f;  cameraElevation = 89f }
            ViewMode.ISOMETRIC -> { cameraAzimuth = 45f; cameraElevation = 35f }
            ViewMode.PERSPECTIVE -> Unit
        }
    }

    // Convert FloorPlan → list of RoomShape-like structs for the renderer
    val closedRooms = remember(floorPlan) {
        floorPlan.rooms.mapIndexed { idx, room ->
            val (floorC, wallC) = ROOM_PALETTES[idx % ROOM_PALETTES.size]
            RoomShape(
                name = "Room ${idx + 1}",
                wallPoints = room.map { floorPlan.nodes[it] },
                isClosed = true,
                floorColor = floorC,
                wallColor = wallC
            )
        }
    }

    val closedRoomsRef    = rememberUpdatedState(closedRooms)
    val furnitureRef      = rememberUpdatedState(placedFurniture)
    val onMoveRef         = rememberUpdatedState(onMoveFurniture)
    val onMoveWallRef     = rememberUpdatedState(onMoveWallFurniture)
    val onSelectRef       = rememberUpdatedState(onSelectFurniture)
    val onEditRef         = rememberUpdatedState(onEditFurniture)
    val viewModeRef       = rememberUpdatedState(viewMode)
    val roomPolygonsRef   = rememberUpdatedState(roomPolygons)

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color(0xFFE8E4DF))
        .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
        .pointerInput(Unit) {
            val hitPx = 48.dp.toPx()
            awaitEachGesture {
                val down    = awaitFirstDown()
                val downPos = down.position

                // Hit-test furniture
                val allPts = closedRoomsRef.value.flatMap { it.wallPoints }
                var hitId: String? = null
                if (allPts.isNotEmpty() && canvasSize != Size.Zero) {
                    val minX = allPts.minOf { it.x }; val maxX = allPts.maxOf { it.x }
                    val minY = allPts.minOf { it.y }; val maxY = allPts.maxOf { it.y }
                    val gCX  = (minX + maxX) / 2f;   val gCZ  = (minY + maxY) / 2f
                    val s3D  = minOf(canvasSize.width, canvasSize.height) /
                               maxOf((maxX - minX).coerceAtLeast(1f), (maxY - minY).coerceAtLeast(1f)) *
                               0.52f * zoom
                    val scCX = canvasSize.width / 2f; val scCY = canvasSize.height / 2f
                    val azRad = Math.toRadians(cameraAzimuth.toDouble())
                    val elRad = Math.toRadians(cameraElevation.toDouble())
                    hitId = furnitureRef.value.minByOrNull { item ->
                        val x = item.posX - gCX; val z = item.posZ - gCZ
                        val y = if (item.isWallMounted) item.wallMountHeight else 20f
                        val rx = x * cos(azRad) + z * sin(azRad)
                        val rz = x * sin(azRad) - z * cos(azRad)
                        val sx = (scCX + rx * s3D).toFloat()
                        val sy = (scCY - (rz * sin(elRad) + y * cos(elRad)) * s3D).toFloat()
                        (Offset(sx, sy) - downPos).getDistance()
                    }?.takeIf { item ->
                        val x = item.posX - gCX; val z = item.posZ - gCZ
                        val y = if (item.isWallMounted) item.wallMountHeight else 20f
                        val rx = x * cos(azRad) + z * sin(azRad)
                        val rz = x * sin(azRad) - z * cos(azRad)
                        val sx = (scCX + rx * s3D).toFloat()
                        val sy = (scCY - (rz * sin(elRad) + y * cos(elRad)) * s3D).toFloat()
                        (Offset(sx, sy) - downPos).getDistance() < hitPx
                    }?.id
                }
                draggingId = hitId

                var totalMove    = 0f
                var lastPos      = downPos
                var prevPinchDist = 0f

                do {
                    val event = awaitPointerEvent()
                    val main  = event.changes.firstOrNull { it.id == down.id } ?: break
                    val extra = event.changes.filter { it.id != down.id && it.pressed }
                    val delta = main.position - lastPos
                    totalMove += delta.getDistance()

                    when {
                        extra.isNotEmpty() -> {
                            val other   = extra.first()
                            val curDist = (main.position - other.position).getDistance()
                            if (prevPinchDist > 0f) zoom = (zoom * curDist / prevPinchDist).coerceIn(0.5f, 3f)
                            prevPinchDist = curDist; draggingId = null
                            main.consume(); other.consume()
                        }
                        hitId != null && totalMove > 4f -> {
                            prevPinchDist = 0f
                            val pts = closedRoomsRef.value.flatMap { it.wallPoints }
                            if (pts.isNotEmpty() && canvasSize != Size.Zero) {
                                val minX = pts.minOf { it.x }; val maxX = pts.maxOf { it.x }
                                val minY = pts.minOf { it.y }; val maxY = pts.maxOf { it.y }
                                val s3D  = minOf(canvasSize.width, canvasSize.height) /
                                           maxOf((maxX - minX).coerceAtLeast(1f), (maxY - minY).coerceAtLeast(1f)) *
                                           0.52f * zoom
                                val azRad = Math.toRadians(cameraAzimuth.toDouble())
                                val elRad = Math.toRadians(cameraElevation.toDouble())
                                val hitItem = furnitureRef.value.find { it.id == hitId }
                                if (hitItem != null) {
                                    if (hitItem.isWallMounted) {
                                        // Wall drag: slide along wall tangent + change height
                                        val wall = findNearestWall(hitItem.posX, hitItem.posZ,
                                                                    roomPolygonsRef.value)
                                        if (wall != null) {
                                            val sinEl = sin(elRad).toFloat().coerceAtLeast(0.05f)
                                            val cosEl = cos(elRad).toFloat().coerceAtLeast(0.05f)
                                            val A = delta.x / s3D
                                            val B = -delta.y / (s3D * sinEl)
                                            val fullDx = (A * cos(azRad) + B * sin(azRad)).toFloat()
                                            val fullDz = (A * sin(azRad) - B * cos(azRad)).toFloat()
                                            val along = fullDx * wall.tangentX + fullDz * wall.tangentZ
                                            val newX = hitItem.posX + along * wall.tangentX
                                            val newZ = hitItem.posZ + along * wall.tangentZ
                                            val snapped = findNearestWall(newX, newZ, roomPolygonsRef.value)
                                            val dH = -delta.y / (s3D * cosEl)
                                            val newH = (hitItem.wallMountHeight + dH).coerceIn(30f, 300f)
                                            onMoveWallRef.value(hitId!!,
                                                snapped?.snappedX ?: newX,
                                                snapped?.snappedZ ?: newZ, newH)
                                        }
                                    } else {
                                        // Floor drag: inverse projection
                                        val sinEl = sin(elRad).toFloat().coerceAtLeast(0.05f)
                                        val A = delta.x / s3D; val B = -delta.y / (s3D * sinEl)
                                        val dx = (A * cos(azRad) + B * sin(azRad)).toFloat()
                                        val dz = (A * sin(azRad) - B * cos(azRad)).toFloat()
                                        onMoveRef.value(hitId!!, hitItem.posX + dx, hitItem.posZ + dz)
                                    }
                                }
                            }
                            main.consume()
                        }
                        else -> {
                            prevPinchDist = 0f
                            if (totalMove > 4f && viewModeRef.value == ViewMode.PERSPECTIVE) {
                                cameraAzimuth   = (cameraAzimuth + delta.x * 0.3f) % 360f
                                cameraElevation = (cameraElevation - delta.y * 0.3f).coerceIn(10f, 88f)
                                main.consume()
                            }
                        }
                    }
                    lastPos = main.position
                } while (event.changes.any { it.pressed })

                draggingId = null
                if (totalMove < 8f) {
                    onSelectRef.value(hitId)
                    if (hitId != null) onEditRef.value(hitId)
                }
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (closedRooms.isEmpty()) return@Canvas
            drawAllRooms3D(
                rooms = closedRooms,
                roomHeight = roomHeight,
                furniture = placedFurniture,
                selectedId = selectedId ?: draggingId,
                azimuth = cameraAzimuth,
                elevation = cameraElevation,
                zoom = zoom,
                canvasSize = size,
                roomPolygons = roomPolygons,
                openings = floorPlan.openings
            )
        }

        AnimatedVisibility(visible = draggingId != null,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            enter = fadeIn(), exit = fadeOut()) {
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 4.dp) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.OpenWith, null, Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Dragging", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        Column(Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallFloatingActionButton(onClick = { zoom = (zoom * 1.2f).coerceAtMost(3f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) { Icon(Icons.Filled.ZoomIn, "Zoom In", Modifier.size(18.dp)) }
            SmallFloatingActionButton(onClick = { zoom = (zoom * 0.8f).coerceAtLeast(0.5f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) { Icon(Icons.Filled.ZoomOut, "Zoom Out", Modifier.size(18.dp)) }
            SmallFloatingActionButton(onClick = { cameraAzimuth = 35f; cameraElevation = 50f; zoom = 1f },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) { Icon(Icons.Filled.CenterFocusWeak, "Reset", Modifier.size(18.dp)) }
        }

        Surface(Modifier.align(Alignment.TopEnd).padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), tonalElevation = 4.dp) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("${closedRooms.size} rooms",
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                val totalArea = closedRooms.sumOf { polygonArea(it.wallPoints).toDouble() }
                Text("%.1f m² total".format(totalArea / 10_000),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── 3D Multi-room Renderer ───────────────────────────────────────────────────

fun DrawScope.drawAllRooms3D(
    rooms: List<RoomShape>,
    roomHeight: Float,
    furniture: List<PlacedFurniture>,
    selectedId: String?,
    azimuth: Float,
    elevation: Float,
    zoom: Float,
    canvasSize: Size,
    roomPolygons: List<List<WallPoint>> = emptyList(),
    openings: List<WallOpening> = emptyList()
) {
    val allPoints = rooms.flatMap { it.wallPoints }
    if (allPoints.isEmpty()) return

    val minX = allPoints.minOf { it.x }; val maxX = allPoints.maxOf { it.x }
    val minY = allPoints.minOf { it.y }; val maxY = allPoints.maxOf { it.y }
    val spanW = (maxX - minX).coerceAtLeast(1f); val spanL = (maxY - minY).coerceAtLeast(1f)
    val globalCX = (minX + maxX) / 2f;           val globalCZ = (minY + maxY) / 2f
    val scale = minOf(canvasSize.width, canvasSize.height) / maxOf(spanW, spanL) * 0.52f * zoom
    val cx = canvasSize.width / 2f;               val cy = canvasSize.height / 2f
    val azRad = Math.toRadians(azimuth.toDouble())
    val elRad = Math.toRadians(elevation.toDouble())

    // z is passed in 2D canvas convention (y-down = near), so negate it for standard 3D depth
    fun project(x: Float, y: Float, z: Float): Offset {
        val rx = x * cos(azRad) + z * sin(azRad)
        val rz = x * sin(azRad) - z * cos(azRad)
        return Offset(
            cx + (rx * scale).toFloat(),
            cy - ((rz * sin(elRad) + y * cos(elRad)) * scale).toFloat()
        )
    }
    fun projectPt(pt: WallPoint, yH: Float = 0f) = project(pt.x - globalCX, yH, pt.y - globalCZ)

    rooms.forEachIndexed { roomIdx, room ->
        val pts = room.wallPoints

        // Floor
        val floorPath = Path().apply {
            val s = projectPt(pts.first()); moveTo(s.x, s.y)
            pts.drop(1).forEach { p -> val sp = projectPt(p); lineTo(sp.x, sp.y) }
            close()
        }
        drawPath(floorPath, room.floorColor.copy(alpha = 0.78f))
        drawPath(floorPath, Color.Black.copy(alpha = 0.10f), style = Stroke(1f))

        // Floor grid
        val rMinX = pts.minOf { it.x }; val rMaxX = pts.maxOf { it.x }
        val rMinY = pts.minOf { it.y }; val rMaxY = pts.maxOf { it.y }
        val rW = rMaxX - rMinX; val rL = rMaxY - rMinY
        for (i in 0..5) {
            val t = i / 5f
            drawLine(Color.Black.copy(alpha = 0.06f),
                project(rMinX + rW * t - globalCX, 0f, rMinY - globalCZ),
                project(rMinX + rW * t - globalCX, 0f, rMaxY - globalCZ), 0.7f)
            drawLine(Color.Black.copy(alpha = 0.06f),
                project(rMinX - globalCX, 0f, rMinY + rL * t - globalCZ),
                project(rMaxX - globalCX, 0f, rMinY + rL * t - globalCZ), 0.7f)
        }

        // Walls — back to front, with door/window openings
        data class WallSeg(val a: WallPoint, val b: WallPoint, val depth: Float, val edgeIdx: Int)
        val segments = pts.indices.map { i ->
            val a = pts[i]; val b = pts[(i + 1) % pts.size]
            WallSeg(a, b, ((a.x + b.x) / 2f - globalCX) * sin(azRad).toFloat() +
                          ((a.y + b.y) / 2f - globalCZ) * cos(azRad).toFloat(), i)
        }.sortedByDescending { it.depth }

        segments.forEach { wallSeg ->
            val (a, b, _, edgeIdx) = wallSeg
            val dx = b.x - a.x; val dz = b.y - a.y
            val len = sqrt(dx * dx + dz * dz).coerceAtLeast(0.001f)
            val dot = (dz * sin(azRad).toFloat() + (-dx) * cos(azRad).toFloat()) / len
            val bright    = if (dot < 0f) 1.0f else 0.72f
            val wallAlpha = if (dot < 0f) 0.88f else 0.55f

            fun drawWallSection(t0: Float, t1: Float, yBot: Float, yTop: Float) {
                if (t0 >= t1 || yBot >= yTop) return
                val p0 = WallPoint(a.x + dx * t0, a.y + dz * t0)
                val p1 = WallPoint(a.x + dx * t1, a.y + dz * t1)
                val bl2 = projectPt(p0, yBot); val br2 = projectPt(p1, yBot)
                val tr2 = projectPt(p1, yTop); val tl2 = projectPt(p0, yTop)
                val wp = Path().apply {
                    moveTo(tl2.x, tl2.y); lineTo(tr2.x, tr2.y)
                    lineTo(br2.x, br2.y); lineTo(bl2.x, bl2.y); close()
                }
                drawPath(wp, (room.wallColor * bright).copy(alpha = wallAlpha))
                drawPath(wp, Color.Black.copy(alpha = 0.07f), style = Stroke(1f))
            }

            val edgeOpenings = openings.filter { it.roomIdx == roomIdx && it.edgeIdx == edgeIdx }
                .sortedBy { it.t }

            if (edgeOpenings.isEmpty()) {
                drawWallSection(0f, 1f, 0f, roomHeight)
            } else {
                var tPrev = 0f
                for (op in edgeOpenings) {
                    val halfT  = (op.widthCm / 2f) / len.coerceAtLeast(1f)
                    val tStart = (op.t - halfT).coerceAtLeast(0f)
                    val tEnd   = (op.t + halfT).coerceAtMost(1f)
                    if (tStart > tPrev) drawWallSection(tPrev, tStart, 0f, roomHeight)
                    when (op.type) {
                        OpeningType.DOOR -> { /* full-height gap — draw nothing */ }
                        OpeningType.WINDOW -> {
                            drawWallSection(tStart, tEnd, 0f, 90f)             // below sill
                            drawWallSection(tStart, tEnd, 210f, roomHeight)    // above header
                        }
                    }
                    tPrev = tEnd
                }
                if (tPrev < 1f) drawWallSection(tPrev, 1f, 0f, roomHeight)
            }
        }
    }

    // Split: floor items first (below wall items visually), then wall items on top
    furniture.filter { !it.isWallMounted }.forEach { item ->
        drawFurnitureBox(item, selectedId, ::project, scale, globalCX, globalCZ, roomPolygons)
    }
    furniture.filter { it.isWallMounted }.forEach { item ->
        drawFurnitureBox(item, selectedId, ::project, scale, globalCX, globalCZ, roomPolygons)
    }
}

// ─── Furniture Box ────────────────────────────────────────────────────────────

fun DrawScope.drawFurnitureBox(
    item: PlacedFurniture,
    selectedId: String?,
    project: (Float, Float, Float) -> Offset,
    scale: Float,
    roomCX: Float,
    roomCZ: Float,
    roomPolygons: List<List<WallPoint>> = emptyList()
) {
    val isSelected = item.id == selectedId
    val baseColor  = item.colorOverride?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
    } ?: Color(0xFF8B7355)

    val x = item.posX - roomCX; val z = item.posZ - roomCZ

    if (item.isWallMounted) {
        // ── Wall-mounted panel (mirror, TV, picture frame, etc.) ──────────────
        val wall = findNearestWall(item.posX, item.posZ, roomPolygons)
        if (wall != null) {
            val y   = item.wallMountHeight
            val hw  = (if (item.customWidthCm  > 0f) item.customWidthCm  / 2f else 44f) * item.scale
            val hh  = (if (item.customHeightCm > 0f) item.customHeightCm / 2f else 30f) * item.scale
            val dep = (if (item.customDepthCm  > 0f) item.customDepthCm        else 7f) * item.scale
            val tx = wall.tangentX; val tz = wall.tangentZ
            val nx = wall.normalX;  val nz = wall.normalZ
            val wx = wall.snappedX - roomCX
            val wz = wall.snappedZ - roomCZ

            // Front face corners (sticking out into room by `dep`)
            val ftl = project(wx + nx*dep - tx*hw, y + hh, wz + nz*dep - tz*hw)
            val ftr = project(wx + nx*dep + tx*hw, y + hh, wz + nz*dep + tz*hw)
            val fbr = project(wx + nx*dep + tx*hw, y - hh, wz + nz*dep + tz*hw)
            val fbl = project(wx + nx*dep - tx*hw, y - hh, wz + nz*dep - tz*hw)

            // Back face corners (flush against wall)
            val btl = project(wx - tx*hw, y + hh, wz - tz*hw)
            val btr = project(wx + tx*hw, y + hh, wz + tz*hw)
            val bbr = project(wx + tx*hw, y - hh, wz + tz*hw)
            val bbl = project(wx - tx*hw, y - hh, wz - tz*hw)

            val topPath = Path().apply {
                moveTo(btl.x, btl.y); lineTo(btr.x, btr.y)
                lineTo(ftr.x, ftr.y); lineTo(ftl.x, ftl.y); close()
            }
            val botPath = Path().apply {
                moveTo(bbl.x, bbl.y); lineTo(bbr.x, bbr.y)
                lineTo(fbr.x, fbr.y); lineTo(fbl.x, fbl.y); close()
            }
            val leftPath = Path().apply {
                moveTo(btl.x, btl.y); lineTo(bbl.x, bbl.y)
                lineTo(fbl.x, fbl.y); lineTo(ftl.x, ftl.y); close()
            }
            val rightPath = Path().apply {
                moveTo(btr.x, btr.y); lineTo(bbr.x, bbr.y)
                lineTo(fbr.x, fbr.y); lineTo(ftr.x, ftr.y); close()
            }
            val frontPath = Path().apply {
                moveTo(ftl.x, ftl.y); lineTo(ftr.x, ftr.y)
                lineTo(fbr.x, fbr.y); lineTo(fbl.x, fbl.y); close()
            }

            drawPath(topPath,   (baseColor * 0.72f).copy(alpha = 0.75f))
            drawPath(botPath,   (baseColor * 0.65f).copy(alpha = 0.70f))
            drawPath(leftPath,  (baseColor * 0.78f).copy(alpha = 0.72f))
            drawPath(rightPath, (baseColor * 0.78f).copy(alpha = 0.72f))
            drawPath(frontPath, baseColor.copy(alpha = 0.96f))
            drawPath(frontPath, Color.Black.copy(alpha = 0.07f), style = Stroke(1f))

            // Inner screen/glass effect
            val inset = minOf(hw, hh) * 0.18f
            val sftl = project(wx + nx*dep - tx*(hw-inset), y + hh - inset, wz + nz*dep - tz*(hw-inset))
            val sftr = project(wx + nx*dep + tx*(hw-inset), y + hh - inset, wz + nz*dep + tz*(hw-inset))
            val sfbr = project(wx + nx*dep + tx*(hw-inset), y - hh + inset, wz + nz*dep + tz*(hw-inset))
            val sfbl = project(wx + nx*dep - tx*(hw-inset), y - hh + inset, wz + nz*dep - tz*(hw-inset))
            val screenPath = Path().apply {
                moveTo(sftl.x, sftl.y); lineTo(sftr.x, sftr.y)
                lineTo(sfbr.x, sfbr.y); lineTo(sfbl.x, sfbl.y); close()
            }
            drawPath(screenPath, Color.White.copy(alpha = 0.14f))

            if (isSelected) {
                drawPath(frontPath, Color(0xFFFFB74D), style = Stroke(2.5f))
            }
        }
        return
    }

    // ── Floor-placed furniture (box) ──────────────────────────────────────────
    val bw = (if (item.customWidthCm  > 0f) item.customWidthCm  / 2f else 30f) * item.scale
    val bh = (if (item.customHeightCm > 0f) item.customHeightCm       else 40f) * item.scale
    val bl = (if (item.customDepthCm  > 0f) item.customDepthCm  / 2f else 30f) * item.scale
    val rotRad = Math.toRadians(item.rotationY.toDouble())
    val cosR = cos(rotRad).toFloat(); val sinR = sin(rotRad).toFloat()
    fun rX(dx: Float, dz: Float) = dx * cosR - dz * sinR
    fun rZ(dx: Float, dz: Float) = dx * sinR + dz * cosR
    val c1x = x + rX(-bw, -bl); val c1z = z + rZ(-bw, -bl)
    val c2x = x + rX(+bw, -bl); val c2z = z + rZ(+bw, -bl)
    val c3x = x + rX(+bw, +bl); val c3z = z + rZ(+bw, +bl)
    val c4x = x + rX(-bw, +bl); val c4z = z + rZ(-bw, +bl)

    fun quad(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float,
             x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float) = Path().apply {
        val a = project(x1, y1, z1); val b = project(x2, y2, z2)
        val c = project(x3, y3, z3); val d = project(x4, y4, z4)
        moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close()
    }
    drawPath(quad(c1x, bh, c1z, c2x, bh, c2z, c3x, bh, c3z, c4x, bh, c4z), baseColor.copy(alpha = 0.92f))
    drawPath(quad(c4x, bh, c4z, c3x, bh, c3z, c3x, 0f, c3z, c4x, 0f, c4z), baseColor.copy(alpha = 0.70f))
    drawPath(quad(c2x, bh, c2z, c3x, bh, c3z, c3x, 0f, c3z, c2x, 0f, c2z), baseColor.copy(alpha = 0.55f))
    if (isSelected)
        drawPath(quad(c1x, bh, c1z, c2x, bh, c2z, c3x, bh, c3z, c4x, bh, c4z),
            Color(0xFFFFB74D), style = Stroke(2.5f))
}
