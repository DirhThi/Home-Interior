package com.interiordesign3d.ui.screen.designer.view.viewport

import com.interiordesign3d.data.catalog.*
import com.interiordesign3d.ui.screen.designer.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import kotlin.math.*

// ─── Wall Drawing Canvas ──────────────────────────────────────────────────────

@Composable
fun WallDrawingCanvas(
    floorPlan: FloorPlan,
    drawingPhase: DrawingPhase,
    currentPath: List<Int>,
    gridSizeCm: Float,
    placementTool: PlacementTool = PlacementTool.NONE,
    onAddNewPoint: (WallPoint) -> Unit,
    onSnapToNode: (Int) -> Unit,
    onClosePath: () -> Unit,
    onMoveNode: (Int, WallPoint) -> Unit,
    onStartFromNode: (Int) -> Unit,
    onStartNewPoint: (WallPoint) -> Unit,
    onPlaceOpening: (roomIdx: Int, edgeIdx: Int, t: Float, type: OpeningType) -> Unit = { _, _, _, _ -> },
    onMoveOpening: (id: String, t: Float) -> Unit = { _, _ -> },
    onResizeOpening: (id: String, newWidthCm: Float) -> Unit = { _, _ -> },
    onRemoveOpening: (id: String) -> Unit = {},
    placedFurniture: List<PlacedFurniture> = emptyList(),
    onMoveFurnitureInPlan: (id: String, posX: Float, posZ: Float) -> Unit = { _, _, _ -> },
    onTapFurniture: (id: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val panOffset = remember { mutableStateOf(Offset.Zero) }
    val scale     = remember { mutableFloatStateOf(1.5f) }
    var initialized by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulseRadius"
    )

    // Keep all refs fresh inside pointerInput
    val planRef   = rememberUpdatedState(floorPlan)
    val phaseRef  = rememberUpdatedState(drawingPhase)
    val pathRef   = rememberUpdatedState(currentPath)
    val gridRef   = rememberUpdatedState(gridSizeCm)
    val onAddNew   = rememberUpdatedState(onAddNewPoint)
    val onSnap     = rememberUpdatedState(onSnapToNode)
    val onClose    = rememberUpdatedState(onClosePath)
    val onMove     = rememberUpdatedState(onMoveNode)
    val onFromNode = rememberUpdatedState(onStartFromNode)
    val onNewPt    = rememberUpdatedState(onStartNewPoint)
    val toolRef         = rememberUpdatedState(placementTool)
    val onPlaceOp       = rememberUpdatedState(onPlaceOpening)
    val onMoveOp        = rememberUpdatedState(onMoveOpening)
    val onResizeOp      = rememberUpdatedState(onResizeOpening)
    val onRemoveOp      = rememberUpdatedState(onRemoveOpening)
    val placedFurRef    = rememberUpdatedState(placedFurniture)
    val onMoveFurRef    = rememberUpdatedState(onMoveFurnitureInPlan)
    val onTapFurRef     = rememberUpdatedState(onTapFurniture)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF12121F))
            .onSizeChanged { sz ->
                if (!initialized && sz.width > 0) {
                    scale.floatValue = sz.width * 0.8f / 600f
                    panOffset.value  = Offset(sz.width * 0.1f, sz.height * 0.12f)
                    initialized = true
                }
            }
            .pointerInput(Unit) {
                val hitPx   = 30.dp.toPx()
                val closePx = 38.dp.toPx()

                awaitEachGesture {
                    val down    = awaitFirstDown()
                    val downPos = down.position
                    val pan0    = panOffset.value
                    val sc0     = scale.floatValue

                    fun screenOf(pt: WallPoint) = Offset(panOffset.value.x + pt.x * scale.floatValue,
                                                         panOffset.value.y + pt.y * scale.floatValue)
                    fun toCm(pos: Offset) = WallPoint(
                        ((pos.x - panOffset.value.x) / scale.floatValue).snapTo(gridRef.value),
                        ((pos.y - panOffset.value.y) / scale.floatValue).snapTo(gridRef.value)
                    )

                    // Find nearest existing node at gesture start
                    val plan0       = planRef.value
                    val nearNodeIdx = plan0.nodes.indexOfFirst { pt ->
                        (Offset(pan0.x + pt.x * sc0, pan0.y + pt.y * sc0) - downPos).getDistance() < hitPx
                    }

                    // Find nearest existing opening (for drag)
                    var nearOpening: OpeningHit? = null
                    if (plan0.openings.isNotEmpty()) {
                        var bestOpDist = hitPx * 2f
                        for (op in plan0.openings) {
                            val room  = plan0.rooms.getOrNull(op.roomIdx) ?: continue
                            val aIdx  = room.getOrNull(op.edgeIdx) ?: continue
                            val bIdx  = room.getOrNull((op.edgeIdx + 1) % room.size) ?: continue
                            val a     = plan0.nodes.getOrNull(aIdx) ?: continue
                            val b     = plan0.nodes.getOrNull(bIdx) ?: continue
                            val aS    = Offset(pan0.x + a.x * sc0, pan0.y + a.y * sc0)
                            val bS    = Offset(pan0.x + b.x * sc0, pan0.y + b.y * sc0)
                            val eDx   = bS.x - aS.x; val eDy = bS.y - aS.y
                            val eLen  = sqrt(eDx * eDx + eDy * eDy).coerceAtLeast(0.001f)
                            val eLenCm = sqrt((b.x - a.x).pow(2) + (b.y - a.y).pow(2)).coerceAtLeast(0.001f)
                            val halfWPx = (op.widthCm / 2f) / eLenCm * eLen
                            val opS   = Offset(aS.x + eDx * op.t, aS.y + eDy * op.t)
                            val endL  = Offset(opS.x - (eDx / eLen) * halfWPx, opS.y - (eDy / eLen) * halfWPx)
                            val endR  = Offset(opS.x + (eDx / eLen) * halfWPx, opS.y + (eDy / eLen) * halfWPx)
                            val distEnd = minOf((endL - downPos).getDistance(), (endR - downPos).getDistance())
                            val distCenter = (opS - downPos).getDistance()
                            // End handles take priority if close enough
                            if (distEnd < hitPx * 1.5f && distEnd < bestOpDist) {
                                bestOpDist = distEnd
                                nearOpening = OpeningHit(op.id, a, b, OpeningHitZone.END, eLenCm)
                            } else if (distCenter < bestOpDist) {
                                bestOpDist = distCenter
                                nearOpening = OpeningHit(op.id, a, b, OpeningHitZone.CENTER, eLenCm)
                            }
                        }
                    }

                    // Find nearest furniture item (for drag/tap in EDITING phase)
                    var nearFurniture: PlacedFurniture? = null
                    if (phaseRef.value == DrawingPhase.EDITING) {
                        var bestFurDist = hitPx * 1.6f
                        for (item in placedFurRef.value) {
                            val sx = pan0.x + item.posX * sc0
                            val sy = pan0.y + item.posZ * sc0
                            val dist = (Offset(sx, sy) - downPos).getDistance()
                            if (dist < bestFurDist) { bestFurDist = dist; nearFurniture = item }
                        }
                    }

                    var totalMove     = 0f
                    var lastPos       = downPos
                    var prevPinchDist = 0f

                    do {
                        val event = awaitPointerEvent()
                        val main  = event.changes.firstOrNull { it.id == down.id } ?: break
                        val extra = event.changes.filter { it.id != down.id && it.pressed }
                        val delta = main.position - lastPos
                        totalMove += delta.getDistance()

                        if (extra.isNotEmpty()) {
                            // Pinch zoom
                            val other   = extra.first()
                            val curDist = (main.position - other.position).getDistance()
                            if (prevPinchDist > 0f) {
                                val factor = curDist / prevPinchDist
                                val mid    = (main.position + other.position) / 2f
                                val newSc  = (scale.floatValue * factor).coerceIn(0.2f, 12f)
                                panOffset.value = mid - (mid - panOffset.value) * (newSc / scale.floatValue)
                                scale.floatValue = newSc
                            }
                            prevPinchDist = curDist
                            main.consume(); other.consume()
                        } else {
                            prevPinchDist = 0f
                            if (totalMove > 8f) {
                                when {
                                    nearFurniture != null -> {
                                        // Move furniture in 2D plan
                                        val sc = scale.floatValue
                                        val newX = (main.position.x - panOffset.value.x) / sc
                                        val newZ = (main.position.y - panOffset.value.y) / sc
                                        onMoveFurRef.value(nearFurniture!!.id, newX, newZ)
                                    }
                                    nearOpening != null -> {
                                        val hit = nearOpening!!
                                        val a = hit.aNode; val b = hit.bNode
                                        val aS = Offset(panOffset.value.x + a.x * scale.floatValue,
                                                        panOffset.value.y + a.y * scale.floatValue)
                                        val bS = Offset(panOffset.value.x + b.x * scale.floatValue,
                                                        panOffset.value.y + b.y * scale.floatValue)
                                        val eVec = bS - aS
                                        val eLen  = sqrt(eVec.x * eVec.x + eVec.y * eVec.y).coerceAtLeast(0.001f)
                                        val eLen2 = eLen * eLen
                                        if (hit.zone == OpeningHitZone.CENTER) {
                                            // Move: project finger onto edge → new t
                                            val t = ((main.position - aS).let { it.x * eVec.x + it.y * eVec.y } / eLen2)
                                                .coerceIn(0.05f, 0.95f)
                                            onMoveOp.value(hit.id, t)
                                        } else {
                                            // Resize: distance from center to finger projected onto edge → new halfWidth
                                            val tCenter = ((main.position - aS).let { it.x * eVec.x + it.y * eVec.y } / eLen2)
                                            val op = planRef.value.openings.firstOrNull { it.id == hit.id }
                                            if (op != null) {
                                                val halfPx = abs(tCenter - op.t) * eLen
                                                val newWidthCm = (halfPx / eLen * hit.edgeLenCm * 2f)
                                                onResizeOp.value(hit.id, newWidthCm)
                                            }
                                        }
                                    }
                                    nearNodeIdx >= 0 ->
                                        onMove.value(nearNodeIdx, toCm(main.position))
                                    else ->
                                        panOffset.value += delta
                                }
                                main.consume()
                            }
                            lastPos = main.position
                        }
                    } while (event.changes.any { it.pressed })

                    // ── Tap logic ─────────────────────────────────────────────
                    if (totalMove < 8f) {
                        // Furniture tap takes priority in EDITING phase
                        if (nearFurniture != null && phaseRef.value == DrawingPhase.EDITING) {
                            onTapFurRef.value(nearFurniture!!.id)
                            return@awaitEachGesture
                        }

                        val plan  = planRef.value
                        val phase = phaseRef.value
                        val path  = pathRef.value
                        val tapCm = toCm(downPos)

                        when (phase) {
                            DrawingPhase.PLACING -> {
                                // Priority 1: tap on first point of current path → close polygon
                                val firstPt = plan.nodes.getOrNull(path.firstOrNull() ?: -1)
                                if (firstPt != null && path.size >= 3) {
                                    val firstScreen = screenOf(firstPt)
                                    if ((firstScreen - downPos).getDistance() < closePx) {
                                        onClose.value(); return@awaitEachGesture
                                    }
                                }
                                // Priority 2: snap to any existing node (not the last one added)
                                if (nearNodeIdx >= 0 && nearNodeIdx != path.lastOrNull()) {
                                    onSnap.value(nearNodeIdx)
                                } else if (nearNodeIdx < 0) {
                                    onAddNew.value(tapCm)
                                }
                            }

                            DrawingPhase.CLOSED -> Unit  // wait for Done button

                            DrawingPhase.EDITING -> {
                                if (toolRef.value != PlacementTool.NONE) {
                                    // Placement tool: tap near existing opening → remove; tap wall → place
                                    var removedId: String? = null
                                    val plan = planRef.value
                                    for (op in plan.openings) {
                                        val room = plan.rooms.getOrNull(op.roomIdx) ?: continue
                                        val aIdx = room.getOrNull(op.edgeIdx) ?: continue
                                        val bIdx = room.getOrNull((op.edgeIdx + 1) % room.size) ?: continue
                                        val a = plan.nodes.getOrNull(aIdx) ?: continue
                                        val b = plan.nodes.getOrNull(bIdx) ?: continue
                                        val aS = Offset(panOffset.value.x + a.x * scale.floatValue,
                                                        panOffset.value.y + a.y * scale.floatValue)
                                        val bS = Offset(panOffset.value.x + b.x * scale.floatValue,
                                                        panOffset.value.y + b.y * scale.floatValue)
                                        val opS = Offset(aS.x + (bS.x - aS.x) * op.t,
                                                         aS.y + (bS.y - aS.y) * op.t)
                                        if ((opS - downPos).getDistance() < hitPx * 1.4f) {
                                            removedId = op.id; break
                                        }
                                    }
                                    if (removedId != null) {
                                        onRemoveOp.value(removedId!!)
                                    } else {
                                        // Find nearest wall edge and place opening
                                        var bestDist = 40.dp.toPx()
                                        var pRoomIdx = -1; var pEdgeIdx = -1; var pT = 0f
                                        plan.rooms.forEachIndexed { rIdx, room ->
                                            room.forEachIndexed { eIdx, aNodeIdx ->
                                                val bNodeIdx = room[(eIdx + 1) % room.size]
                                                val a = plan.nodes[aNodeIdx]
                                                val b = plan.nodes[bNodeIdx]
                                                val aS = Offset(panOffset.value.x + a.x * scale.floatValue,
                                                                panOffset.value.y + a.y * scale.floatValue)
                                                val bS = Offset(panOffset.value.x + b.x * scale.floatValue,
                                                                panOffset.value.y + b.y * scale.floatValue)
                                                val eVec = bS - aS
                                                val eLen2 = (eVec.x*eVec.x + eVec.y*eVec.y).coerceAtLeast(0.001f)
                                                val t = ((downPos - aS).let { it.x*eVec.x + it.y*eVec.y } / eLen2)
                                                    .coerceIn(0.05f, 0.95f)
                                                val closest = Offset(aS.x + eVec.x * t, aS.y + eVec.y * t)
                                                val dist = (closest - downPos).getDistance()
                                                if (dist < bestDist) {
                                                    bestDist = dist; pRoomIdx = rIdx; pEdgeIdx = eIdx; pT = t
                                                }
                                            }
                                        }
                                        if (pRoomIdx >= 0) {
                                            val type = if (toolRef.value == PlacementTool.DOOR)
                                                OpeningType.DOOR else OpeningType.WINDOW
                                            onPlaceOp.value(pRoomIdx, pEdgeIdx, pT, type)
                                        }
                                    }
                                } else {
                                    if (nearNodeIdx >= 0) {
                                        onFromNode.value(nearNodeIdx)
                                    } else {
                                        onNewPt.value(tapCm)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // ── Canvas drawing ────────────────────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            val pan = panOffset.value
            val sc  = scale.floatValue
            fun toScreen(pt: WallPoint) = Offset(pan.x + pt.x * sc, pan.y + pt.y * sc)

            // Grid
            val gridStep  = if (gridSizeCm > 0f) gridSizeCm else 50f
            val majorEvery = 5
            val startXi = ((-pan.x / sc - gridStep) / gridStep).toInt()
            val endXi   = (((size.width - pan.x) / sc + gridStep) / gridStep).toInt()
            val startYi = ((-pan.y / sc - gridStep) / gridStep).toInt()
            val endYi   = (((size.height - pan.y) / sc + gridStep) / gridStep).toInt()
            for (xi in startXi..endXi) {
                val sx    = pan.x + xi * gridStep * sc
                val major = xi % majorEvery == 0
                drawLine(Color.White.copy(alpha = if (major) 0.10f else 0.04f),
                    Offset(sx, 0f), Offset(sx, size.height), if (major) 1f else 0.5f)
            }
            for (yi in startYi..endYi) {
                val sy    = pan.y + yi * gridStep * sc
                val major = yi % majorEvery == 0
                drawLine(Color.White.copy(alpha = if (major) 0.10f else 0.04f),
                    Offset(0f, sy), Offset(size.width, sy), if (major) 1f else 0.5f)
            }

            // ── Committed rooms ───────────────────────────────────────────────
            floorPlan.rooms.forEachIndexed { idx, room ->
                val (floorC, wallC) = ROOM_PALETTES[idx % ROOM_PALETTES.size]
                val pts = room.map { toScreen(floorPlan.nodes[it]) }
                val path = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    pts.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(path, floorC.copy(alpha = 0.22f))
                drawPath(path, wallC.copy(alpha = 0.8f), style = Stroke(2.5f))

                // Wall mid-ticks
                for (i in room.indices) {
                    val a = toScreen(floorPlan.nodes[room[i]])
                    val b = toScreen(floorPlan.nodes[room[(i + 1) % room.size]])
                    val mid = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)
                    val dist = (b - a).getDistance().coerceAtLeast(0.001f)
                    val nx = -(b.y - a.y) / dist * 7f
                    val ny =  (b.x - a.x) / dist * 7f
                    drawLine(wallC.copy(alpha = 0.4f),
                        Offset(mid.x - nx, mid.y - ny), Offset(mid.x + nx, mid.y + ny), 1.5f)
                }
            }

            // ── Current open path ─────────────────────────────────────────────
            if (currentPath.size >= 2) {
                for (i in 0 until currentPath.size - 1) {
                    val a = toScreen(floorPlan.nodes[currentPath[i]])
                    val b = toScreen(floorPlan.nodes[currentPath[i + 1]])
                    drawLine(Color.White.copy(alpha = 0.9f), a, b, 2.5f)
                }
            }

            // ── All nodes ─────────────────────────────────────────────────────
            floorPlan.nodes.forEachIndexed { idx, pt ->
                val screen   = toScreen(pt)
                val inPath   = idx in currentPath
                val isFirst  = currentPath.firstOrNull() == idx
                val canClose = isFirst && currentPath.size >= 3 && drawingPhase == DrawingPhase.PLACING

                when {
                    canClose -> {
                        // Pulsing green ring = tap to close
                        drawCircle(Color(0xFF4CAF50).copy(alpha = 0.3f), pulseRadius, screen)
                        drawCircle(Color(0xFF4CAF50), 13f, screen)
                        drawCircle(Color.White, 6f, screen)
                    }
                    inPath -> {
                        drawCircle(Color.White, 11f, screen)
                        drawCircle(Color(0xFF2196F3), 8f, screen)
                    }
                    drawingPhase == DrawingPhase.EDITING -> {
                        // Tappable corner in edit mode — small with ring
                        drawCircle(Color.White.copy(alpha = 0.5f), 9f, screen)
                        drawCircle(Color(0xFF78909C), 5f, screen)
                    }
                    else -> {
                        drawCircle(Color.White.copy(alpha = 0.4f), 7f, screen)
                        drawCircle(Color(0xFF546E7A), 4f, screen)
                    }
                }
            }

            // ── Openings (doors & windows) ─────────────────────────────────────
            floorPlan.openings.forEach { op ->
                val room = floorPlan.rooms.getOrNull(op.roomIdx) ?: return@forEach
                val aIdx = room.getOrNull(op.edgeIdx) ?: return@forEach
                val bIdx = room.getOrNull((op.edgeIdx + 1) % room.size) ?: return@forEach
                val a    = floorPlan.nodes.getOrNull(aIdx) ?: return@forEach
                val b    = floorPlan.nodes.getOrNull(bIdx) ?: return@forEach
                val aS   = toScreen(a); val bS = toScreen(b)
                val eDx  = bS.x - aS.x;  val eDy = bS.y - aS.y
                val eLen = sqrt(eDx * eDx + eDy * eDy).coerceAtLeast(0.001f)
                val dirX = eDx / eLen;    val dirY = eDy / eLen
                val perpX = -dirY;        val perpY = dirX
                val cx2  = aS.x + eDx * op.t; val cy2 = aS.y + eDy * op.t
                val eLenCm = sqrt((b.x - a.x).pow(2) + (b.y - a.y).pow(2)).coerceAtLeast(0.001f)
                val halfWPx = (op.widthCm / 2f) / eLenCm * eLen
                val perpLen = (14f * sc).coerceAtLeast(8f)
                val color = if (op.type == OpeningType.DOOR) Color(0xFFE53935) else Color(0xFF1976D2)
                val endL = Offset(cx2 - dirX * halfWPx, cy2 - dirY * halfWPx)
                val endR = Offset(cx2 + dirX * halfWPx, cy2 + dirY * halfWPx)
                // Gap (dark fill over wall line)
                drawLine(Color(0xFF12121F), endL, endR, 5f)
                // End marks (wall stops)
                drawLine(color, Offset(endL.x - perpX * perpLen * .4f, endL.y - perpY * perpLen * .4f),
                    Offset(endL.x + perpX * perpLen * .4f, endL.y + perpY * perpLen * .4f), 2.5f)
                drawLine(color, Offset(endR.x - perpX * perpLen * .4f, endR.y - perpY * perpLen * .4f),
                    Offset(endR.x + perpX * perpLen * .4f, endR.y + perpY * perpLen * .4f), 2.5f)
                // Resize handle dots at each end
                drawCircle(Color.White, 6f, endL)
                drawCircle(color, 4.5f, endL)
                drawCircle(Color.White, 6f, endR)
                drawCircle(color, 4.5f, endR)
                // Center perpendicular line (thick indicator)
                drawLine(color, Offset(cx2 - perpX * perpLen, cy2 - perpY * perpLen),
                    Offset(cx2 + perpX * perpLen, cy2 + perpY * perpLen),
                    if (op.type == OpeningType.DOOR) 3.5f else 2.5f)
                if (op.type == OpeningType.WINDOW) {
                    // Two window lines parallel to wall
                    val off = perpLen * .35f
                    drawLine(color.copy(alpha = 0.7f),
                        Offset(cx2 - dirX * halfWPx * .8f - perpX * off, cy2 - dirY * halfWPx * .8f - perpY * off),
                        Offset(cx2 + dirX * halfWPx * .8f - perpX * off, cy2 + dirY * halfWPx * .8f - perpY * off), 1.5f)
                    drawLine(color.copy(alpha = 0.7f),
                        Offset(cx2 - dirX * halfWPx * .8f + perpX * off, cy2 - dirY * halfWPx * .8f + perpY * off),
                        Offset(cx2 + dirX * halfWPx * .8f + perpX * off, cy2 + dirY * halfWPx * .8f + perpY * off), 1.5f)
                }

            // ── Furniture footprints (floor + wall items) ─────────────────────
            if (drawingPhase == DrawingPhase.EDITING) {
                val baseColor = Color(0xFF8B7355)
                placedFurniture.forEach { item ->
                    val fcx = pan.x + item.posX * sc
                    val fcy = pan.y + item.posZ * sc
                    if (item.isWallMounted) {
                        // Wall item: draw as thin rectangle on the wall line
                        val hw = ((if (item.customWidthCm > 0f) item.customWidthCm else 88f) / 2f) * sc
                        val depth = 5f.coerceAtLeast(sc * 8f)
                        // Find wall direction at item position for orientation
                        val nearestWall = floorPlan.rooms.flatMapIndexed { rIdx, room ->
                            room.indices.map { eIdx ->
                                val aNode = floorPlan.nodes.getOrNull(room[eIdx])
                                val bNode = floorPlan.nodes.getOrNull(room[(eIdx + 1) % room.size])
                                Triple(rIdx, eIdx, if (aNode != null && bNode != null) Pair(aNode, bNode) else null)
                            }
                        }.minByOrNull { (_, _, edge) ->
                            if (edge == null) Float.MAX_VALUE
                            else {
                                val aS = Offset(pan.x + edge.first.x * sc, pan.y + edge.first.y * sc)
                                val bS = Offset(pan.x + edge.second.x * sc, pan.y + edge.second.y * sc)
                                val eVec = bS - aS; val eLen2 = (eVec.x*eVec.x + eVec.y*eVec.y).coerceAtLeast(0.001f)
                                val t = ((Offset(fcx, fcy) - aS).let { it.x*eVec.x + it.y*eVec.y } / eLen2).coerceIn(0f, 1f)
                                (Offset(aS.x + eVec.x*t, aS.y + eVec.y*t) - Offset(fcx, fcy)).getDistance()
                            }
                        }?.third
                        if (nearestWall != null) {
                            val aS = Offset(pan.x + nearestWall.first.x * sc, pan.y + nearestWall.first.y * sc)
                            val bS = Offset(pan.x + nearestWall.second.x * sc, pan.y + nearestWall.second.y * sc)
                            val eVec = bS - aS; val eLen = sqrt(eVec.x*eVec.x + eVec.y*eVec.y).coerceAtLeast(0.001f)
                            val dirX = eVec.x / eLen; val dirY = eVec.y / eLen
                            val perpX = -dirY; val perpY = dirX
                            val corners = listOf(
                                Offset(fcx - dirX*hw - perpX*depth, fcy - dirY*hw - perpY*depth),
                                Offset(fcx + dirX*hw - perpX*depth, fcy + dirY*hw - perpY*depth),
                                Offset(fcx + dirX*hw + perpX*depth, fcy + dirY*hw + perpY*depth),
                                Offset(fcx - dirX*hw + perpX*depth, fcy - dirY*hw + perpY*depth)
                            )
                            val footPath = Path().apply {
                                moveTo(corners[0].x, corners[0].y)
                                corners.drop(1).forEach { lineTo(it.x, it.y) }; close()
                            }
                            drawPath(footPath, Color(0xFF1976D2).copy(alpha = 0.30f))
                            drawPath(footPath, Color(0xFF1976D2).copy(alpha = 0.80f), style = Stroke(1.5f))
                        }
                    } else {
                        val hw = ((if (item.customWidthCm > 0f) item.customWidthCm else 60f) / 2f) * sc
                        val hd = ((if (item.customDepthCm > 0f) item.customDepthCm else 60f) / 2f) * sc
                        val rot = Math.toRadians(item.rotationY.toDouble())
                        val cosR = cos(rot).toFloat(); val sinR = sin(rot).toFloat()
                        val corners = listOf(
                            Offset(-hw * cosR - hd * sinR, -hw * sinR + hd * cosR),
                            Offset( hw * cosR - hd * sinR,  hw * sinR + hd * cosR),
                            Offset( hw * cosR + hd * sinR,  hw * sinR - hd * cosR),
                            Offset(-hw * cosR + hd * sinR, -hw * sinR - hd * cosR)
                        ).map { Offset(fcx + it.x, fcy + it.y) }
                        val footPath = Path().apply {
                            moveTo(corners[0].x, corners[0].y)
                            corners.drop(1).forEach { lineTo(it.x, it.y) }; close()
                        }
                        drawPath(footPath, baseColor.copy(alpha = 0.28f))
                        drawPath(footPath, baseColor.copy(alpha = 0.75f), style = Stroke(1.5f))
                        drawCircle(baseColor.copy(alpha = 0.6f), 3.5f, Offset(fcx, fcy))
                    }
                }
            }
            }
        }

        // ── Instruction overlay ───────────────────────────────────────────────
        if (floorPlan.nodes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = 4.dp) {
                    Column(Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.TouchApp, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Text("Tap to place wall corners",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Tap the first point (green pulse) to close",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Hold & drag any corner to reshape",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ── Phase badge ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = drawingPhase == DrawingPhase.EDITING && floorPlan.rooms.isNotEmpty(),
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            enter = fadeIn(), exit = fadeOut()
        ) {
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 4.dp) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Edit, null, Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Tap corner → new room  •  Hold → move",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // ── Stats chip ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = floorPlan.nodes.isNotEmpty(),
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            enter = fadeIn() + slideInHorizontally { it }, exit = fadeOut()
        ) {
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f), tonalElevation = 4.dp) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("${floorPlan.rooms.size} room(s)",
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    if (floorPlan.rooms.isNotEmpty()) {
                        val totalArea = floorPlan.rooms.sumOf { room ->
                            polygonArea(room.map { floorPlan.nodes[it] }).toDouble()
                        }
                        Text("%.1f m² total".format(totalArea / 10_000f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    } else if (currentPath.size >= 3) {
                        Text("Tap first point to close",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("${3 - currentPath.size} more corners needed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ── Zoom controls ─────────────────────────────────────────────────────
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallFloatingActionButton(
                onClick = { scale.floatValue = (scale.floatValue * 1.3f).coerceAtMost(12f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) { Icon(Icons.Filled.ZoomIn, "Zoom In", Modifier.size(18.dp)) }
            SmallFloatingActionButton(
                onClick = { scale.floatValue = (scale.floatValue * 0.77f).coerceAtLeast(0.2f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) { Icon(Icons.Filled.ZoomOut, "Zoom Out", Modifier.size(18.dp)) }
        }
    }
}
