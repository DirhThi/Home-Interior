package com.interiordesign3d.ui.screen.designer.view.viewport

import com.interiordesign3d.data.catalog.*
import com.interiordesign3d.ui.screen.designer.*
import com.interiordesign3d.ui.theme.LocalInteriorAccents
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.interiordesign3d.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.shape.CircleShape

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
    onTapOpening: (id: String) -> Unit = {},
    activeLevel: Int = 0,
    onPlaceStair: (x: Float, y: Float) -> Unit = { _, _ -> },
    onMoveStair: (id: String, x: Float, y: Float) -> Unit = { _, _, _ -> },
    onTapStair: (id: String) -> Unit = {},
    selectedStairId: String? = null,
    placedFurniture: List<PlacedFurniture> = emptyList(),
    onMoveFurnitureInPlan: (id: String, posX: Float, posZ: Float) -> Unit = { _, _, _ -> },
    onTapFurniture: (id: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accents = LocalInteriorAccents.current
    val textMeasurer = rememberTextMeasurer()
    val errorColor = MaterialTheme.colorScheme.error
    val dimensionStyle = MaterialTheme.typography.labelSmall.copy(color = accents.dimensionText)
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
    val onTapOp         = rememberUpdatedState(onTapOpening)
    val levelRef        = rememberUpdatedState(activeLevel)
    val onPlaceStairRef = rememberUpdatedState(onPlaceStair)
    val onMoveStairRef  = rememberUpdatedState(onMoveStair)
    val onTapStairRef   = rememberUpdatedState(onTapStair)
    val placedFurRef    = rememberUpdatedState(placedFurniture)
    val onMoveFurRef    = rememberUpdatedState(onMoveFurnitureInPlan)
    val onTapFurRef     = rememberUpdatedState(onTapFurniture)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(accents.canvasBackground)
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
                        for (op in plan0.openings.filter { it.level == levelRef.value }) {
                            val aIdx  = op.nodeA
                            val bIdx  = op.nodeB
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

                    // Stair under the finger (for drag/tap)
                    var nearStair: Stair? = null
                    for (st in plan0.stairs) {
                        if (st.level != levelRef.value) continue
                        val poly = st.footprint().map { Offset(pan0.x + it.x * sc0, pan0.y + it.y * sc0) }
                        if (pointInScreenPoly(downPos, poly)) { nearStair = st; break }
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
                                    nearStair != null -> {
                                        val c = toCm(main.position)
                                        onMoveStairRef.value(nearStair!!.id, c.x, c.y)
                                    }
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
                        val stair = nearStair
                        if (stair != null && toolRef.value == PlacementTool.NONE) {
                            onTapStairRef.value(stair.id)
                            return@awaitEachGesture
                        }
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
                                if (toolRef.value == PlacementTool.STAIRS) {
                                    onPlaceStairRef.value(tapCm.x, tapCm.y)
                                } else if (toolRef.value != PlacementTool.NONE) {
                                    // Placement tool: tap near existing opening → remove; tap wall → place
                                    var removedId: String? = null
                                    val plan = planRef.value
                                    for (op in plan.openings.filter { it.level == levelRef.value }) {
                                        val aIdx = op.nodeA
                                        val bIdx = op.nodeB
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
                                    // No tool: an opening under the finger is being selected, not replaced.
                                    var tappedId: String? = null
                                    for (op in plan.openings.filter { it.level == levelRef.value }) {
                                        val a = plan.nodes.getOrNull(op.nodeA) ?: continue
                                        val b = plan.nodes.getOrNull(op.nodeB) ?: continue
                                        val aS = screenOf(a); val bS = screenOf(b)
                                        val opS = Offset(aS.x + (bS.x - aS.x) * op.t,
                                                         aS.y + (bS.y - aS.y) * op.t)
                                        if ((opS - downPos).getDistance() < hitPx * 1.4f) {
                                            tappedId = op.id; break
                                        }
                                    }
                                    when {
                                        tappedId != null -> onTapOp.value(tappedId!!)
                                        nearNodeIdx >= 0 -> onFromNode.value(nearNodeIdx)
                                        else -> onNewPt.value(tapCm)
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
                drawLine(if (major) accents.canvasGridMajor else accents.canvasGrid,
                    Offset(sx, 0f), Offset(sx, size.height), if (major) 1f else 0.5f)
            }
            for (yi in startYi..endYi) {
                val sy    = pan.y + yi * gridStep * sc
                val major = yi % majorEvery == 0
                drawLine(if (major) accents.canvasGridMajor else accents.canvasGrid,
                    Offset(0f, sy), Offset(size.width, sy), if (major) 1f else 0.5f)
            }

            // A shared wall belongs to two rooms; without this both label it and the two
            // dimensions land on top of each other.
            val labelledEdges = HashSet<Long>()

            // ── Storey below: dashed, so it reads as a guide to line up against rather than
            //    part of the plan being edited ───────────────────────────────────────────
            if (activeLevel > 0) {
                val dashed = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                )
                floorPlan.rooms.forEachIndexed { idx, room ->
                    if (floorPlan.levelOf(idx) != activeLevel - 1) return@forEachIndexed
                    val pts = room.map { toScreen(floorPlan.nodes[it]) }
                    if (pts.size < 2) return@forEachIndexed
                    val guide = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        pts.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }
                    drawPath(guide, accents.canvasWall.copy(alpha = 0.32f), style = dashed)
                }
            }

            // ── Committed rooms ───────────────────────────────────────────────
            floorPlan.rooms.forEachIndexed { idx, room ->
                if (floorPlan.levelOf(idx) != activeLevel) return@forEachIndexed
                val floorC = accents.canvasRoomFill
                val wallC = accents.canvasWall
                val pts = room.map { toScreen(floorPlan.nodes[it]) }
                val roomCentroid = Offset(
                    pts.map { it.x }.average().toFloat(),
                    pts.map { it.y }.average().toFloat(),
                )
                val path = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    pts.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(path, floorC)
                drawPath(path, wallC.copy(alpha = 0.85f), style = Stroke(2.5f))

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

                    val n0 = room[i]
                    val n1 = room[(i + 1) % room.size]
                    val edgeKey = minOf(n0, n1).toLong() * 100_000L + maxOf(n0, n1)
                    if (labelledEdges.add(edgeKey)) {
                        drawDimension(
                            textMeasurer = textMeasurer,
                            style = dimensionStyle,
                            chip = accents.dimensionChip,
                            from = floorPlan.nodes[n0],
                            to = floorPlan.nodes[n1],
                            screenFrom = a,
                            screenTo = b,
                            awayFrom = roomCentroid,
                        )
                    }
                }
            }

            // ── Stairs on this storey ────────────────────────────────────────
            floorPlan.stairs.forEach { st ->
                if (st.level != activeLevel) return@forEach
                val corners = st.footprint().map { toScreen(it) }
                val outline = Path().apply {
                    moveTo(corners.first().x, corners.first().y)
                    corners.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                val selected = st.id == selectedStairId
                val tone = if (floorPlan.stairFits(st)) accents.dimensionText else errorColor
                drawPath(outline, tone.copy(alpha = 0.16f))
                drawPath(
                    outline,
                    tone.copy(alpha = if (selected) 1f else 0.7f),
                    style = Stroke(if (selected) 3f else 2f),
                )
                // Treads belong to the straight runs; the landing between them stays blank,
                // which is how a turning flight reads on a drawing.
                val halfW = st.widthCm * sc / 2f
                st.runs().forEach { (ra, rb) ->
                    val p = toScreen(ra); val q = toScreen(rb)
                    val len = hypot(q.x - p.x, q.y - p.y)
                    if (len < 1f) return@forEach
                    val ux = (q.x - p.x) / len; val uy = (q.y - p.y) / len
                    val treads = (len / (26f * sc / 1.5f)).toInt().coerceIn(3, 14)
                    for (k in 1 until treads) {
                        val d = len * k / treads
                        val cxp = p.x + ux * d; val cyp = p.y + uy * d
                        drawLine(
                            tone.copy(alpha = 0.5f),
                            Offset(cxp - uy * halfW, cyp + ux * halfW),
                            Offset(cxp + uy * halfW, cyp - ux * halfW),
                            1.5f,
                        )
                    }
                }
            }

            // ── Current open path ─────────────────────────────────────────────
            if (currentPath.size >= 2) {
                for (i in 0 until currentPath.size - 1) {
                    val a = toScreen(floorPlan.nodes[currentPath[i]])
                    val b = toScreen(floorPlan.nodes[currentPath[i + 1]])
                    drawLine(accents.canvasWall.copy(alpha = 0.9f), a, b, 2.5f)
                    // The just-closed room still sits in currentPath; without this its walls get
                    // a second label stacked on the committed one.
                    val c0 = currentPath[i]; val c1 = currentPath[i + 1]
                    val key = minOf(c0, c1).toLong() * 100_000L + maxOf(c0, c1)
                    if (labelledEdges.add(key)) {
                        drawDimension(
                            textMeasurer = textMeasurer,
                            style = dimensionStyle,
                            chip = accents.dimensionChip,
                            from = floorPlan.nodes[c0],
                            to = floorPlan.nodes[c1],
                            screenFrom = a,
                            screenTo = b,
                        )
                    }
                }
            }

            // ── All nodes ─────────────────────────────────────────────────────
            val nodesOnLevel = floorPlan.rooms.indices
                .filter { floorPlan.levelOf(it) == activeLevel }
                .flatMap { floorPlan.rooms[it] }
                .toSet() + currentPath
            floorPlan.nodes.forEachIndexed { idx, pt ->
                if (floorPlan.rooms.isNotEmpty() && idx !in nodesOnLevel) return@forEachIndexed
                val screen   = toScreen(pt)
                val inPath   = idx in currentPath
                val isFirst  = currentPath.firstOrNull() == idx
                val canClose = isFirst && currentPath.size >= 3 && drawingPhase == DrawingPhase.PLACING

                when {
                    canClose -> {
                        // Pulsing green ring = tap to close
                        drawCircle(accents.canvasNodeStart.copy(alpha = 0.3f), pulseRadius, screen)
                        drawCircle(accents.canvasNodeStart, 13f, screen)
                        drawCircle(accents.canvasBackground, 6f, screen)
                    }
                    inPath -> {
                        drawCircle(accents.canvasBackground, 11f, screen)
                        drawCircle(accents.canvasNodeActive, 8f, screen)
                    }
                    drawingPhase == DrawingPhase.EDITING -> {
                        // Tappable corner in edit mode — small with ring
                        drawCircle(accents.canvasBackground.copy(alpha = 0.8f), 9f, screen)
                        drawCircle(accents.canvasNodeIdle, 5f, screen)
                    }
                    else -> {
                        drawCircle(accents.canvasBackground.copy(alpha = 0.7f), 7f, screen)
                        drawCircle(accents.canvasNodeIdle.copy(alpha = 0.7f), 4f, screen)
                    }
                }
            }

            // ── Openings (doors & windows) ─────────────────────────────────────
            floorPlan.openings.filter { it.level == activeLevel }.forEach { op ->
                val aIdx = op.nodeA
                val bIdx = op.nodeB
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
                val color = if (op.type == OpeningType.DOOR) accents.door else accents.window
                val endL = Offset(cx2 - dirX * halfWPx, cy2 - dirY * halfWPx)
                val endR = Offset(cx2 + dirX * halfWPx, cy2 + dirY * halfWPx)
                // Gap (dark fill over wall line)
                drawLine(accents.canvasBackground, endL, endR, 5f)
                // End marks (wall stops)
                drawLine(color, Offset(endL.x - perpX * perpLen * .4f, endL.y - perpY * perpLen * .4f),
                    Offset(endL.x + perpX * perpLen * .4f, endL.y + perpY * perpLen * .4f), 2.5f)
                drawLine(color, Offset(endR.x - perpX * perpLen * .4f, endR.y - perpY * perpLen * .4f),
                    Offset(endR.x + perpX * perpLen * .4f, endR.y + perpY * perpLen * .4f), 2.5f)
                // Resize handle dots at each end
                drawCircle(accents.canvasBackground, 6f, endL)
                drawCircle(color, 4.5f, endL)
                drawCircle(accents.canvasBackground, 6f, endR)
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
                val baseColor = accents.canvasFurniture
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
                            drawPath(footPath, accents.canvasNodeActive.copy(alpha = 0.30f))
                            drawPath(footPath, accents.canvasNodeActive.copy(alpha = 0.80f), style = Stroke(1.5f))
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

        PlanStatusPill(
            floorPlan = floorPlan,
            currentPath = currentPath,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
        )
    }
}

/** The canvas's only chrome: room count + area once a room exists, otherwise the next thing to do. */
@Composable
private fun PlanStatusPill(
    floorPlan: FloorPlan,
    currentPath: List<Int>,
    modifier: Modifier = Modifier,
) {
    val roomCount = floorPlan.rooms.size
    val text = when {
        roomCount > 0 -> {
            val area = floorPlan.rooms.sumOf { room ->
                polygonArea(room.map { floorPlan.nodes[it] }).toDouble()
            } / 10_000.0
            pluralStringResource(R.plurals.plan_rooms_area, roomCount, roomCount, area)
        }
        currentPath.size >= 3 -> stringResource(R.string.plan_hint_close)
        currentPath.isNotEmpty() ->
            pluralStringResource(R.plurals.plan_hint_more, 3 - currentPath.size, 3 - currentPath.size)
        else -> stringResource(R.string.plan_hint_empty)
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        tonalElevation = 2.dp,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

// ─── Wall dimensions ──────────────────────────────────────────────────────────

/** Centimetres between two plan nodes. */
private fun distanceCm(from: WallPoint, to: WallPoint): Float =
    hypot(to.x - from.x, to.y - from.y)

private fun formatLength(cm: Float): String =
    if (cm >= 100f) String.format("%.2f m", cm / 100f) else "${cm.roundToInt()} cm"

/**
 * Draws the wall length on a chip at the segment midpoint, nudged onto the outside of the
 * wall so it never sits on top of the stroke. Skipped for segments too short to read.
 */
private fun DrawScope.drawDimension(
    textMeasurer: TextMeasurer,
    style: TextStyle,
    chip: Color,
    from: WallPoint,
    to: WallPoint,
    screenFrom: Offset,
    screenTo: Offset,
    awayFrom: Offset? = null,
) {
    val screenLen = (screenTo - screenFrom).getDistance()
    if (screenLen < 48f) return

    val label = formatLength(distanceCm(from, to))
    val measured = textMeasurer.measure(label, style)
    val w = measured.size.width.toFloat()
    val h = measured.size.height.toFloat()
    if (w + 12f > screenLen) return

    val mid = Offset((screenFrom.x + screenTo.x) / 2f, (screenFrom.y + screenTo.y) / 2f)
    var nx = -(screenTo.y - screenFrom.y) / screenLen
    var ny = (screenTo.x - screenFrom.x) / screenLen
    val mid0 = Offset((screenFrom.x + screenTo.x) / 2f, (screenFrom.y + screenTo.y) / 2f)
    if (awayFrom != null && nx * (awayFrom.x - mid0.x) + ny * (awayFrom.y - mid0.y) > 0f) {
        nx = -nx
        ny = -ny
    }
    // Clear the wall by the chip's own half-extent along the normal: a vertical wall has to be
    // cleared by half the chip's WIDTH, a horizontal one by half its height.
    val padX = 6f
    val padY = 2f
    val clearance = abs(nx) * (w / 2f + padX) + abs(ny) * (h / 2f + padY) + 7f
    val center = Offset(mid.x + nx * clearance, mid.y + ny * clearance)
    drawRoundRect(
        color = chip,
        topLeft = Offset(center.x - w / 2f - padX, center.y - h / 2f - padY),
        size = Size(w + padX * 2, h + padY * 2),
        cornerRadius = CornerRadius(h / 2f + padY),
    )
    drawText(measured, topLeft = Offset(center.x - w / 2f, center.y - h / 2f))
}

private fun pointInScreenPoly(p: Offset, poly: List<Offset>): Boolean {
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val a = poly[i]; val b = poly[j]
        if ((a.y > p.y) != (b.y > p.y) && p.x < (b.x - a.x) * (p.y - a.y) / (b.y - a.y) + a.x) inside = !inside
        j = i
    }
    return inside
}
