package com.interiordesign3d.ui.screen.home.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.OpeningType
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.models.WallPoint
import com.interiordesign3d.ui.theme.LocalInteriorAccents
import kotlin.math.hypot

// Axonometric projection: x runs down-right, y down-left, z straight up. Screen depth is x + y, so
// that sum doubles as the painter's-algorithm key.
private const val ISO_X = 0.866f
private const val ISO_Y = 0.5f
private const val WALL_H_CM = 270f
private const val STOREY_CM = 280f
private const val FURN_H_CM = 45f
private const val WALL_T_CM = 14f      // exaggerated: the visible top edge is what kills the Necker flip
private const val SLAB_T_CM = 18f
private const val DOOR_TOP = 0.78f
private const val WINDOW_BASE = 0.28f
private const val WINDOW_TOP = 0.83f

private fun iso(x: Float, y: Float, z: Float) =
    Offset((x - y) * ISO_X, (x + y) * ISO_Y - z)

/**
 * The plan as a little doll's house: floors, walls stood up, doors and windows punched through, and
 * the furniture that is actually in the room. Walls facing the viewer are dropped so you can see in,
 * the same trick the 3D viewport plays with `autoHideWalls`.
 *
 * Deliberately Canvas and not a render: it costs a few dozen polygons, needs no cache, and can never
 * show a stale picture because it is drawn from the plan every time.
 */
@Composable
fun PlanThumbnail(
    plan: FloorPlan,
    modifier: Modifier = Modifier,
    furniture: List<PlacedFurniture> = emptyList(),
) {
    val accents = LocalInteriorAccents.current
    val floorFill = accents.thumbFloor
    val faceTone = accents.thumbWallFace
    val sideTone = accents.thumbWallSide
    val topTone = accents.thumbWallTop
    val slabTone = accents.thumbSlab
    val furnTone = accents.thumbFurniture
    val doorTone = accents.thumbDoor
    val windowTone = accents.window

    Canvas(modifier) {
        val nodes = plan.nodes
        if (nodes.isEmpty() || plan.rooms.isEmpty()) return@Canvas

        // Every edge, with the rooms that use it: one user means it is on the outside.
        val uses = HashMap<Long, MutableList<Int>>()
        plan.rooms.forEachIndexed { ri, room ->
            room.indices.forEach { i ->
                val a = room[i]; val b = room[(i + 1) % room.size]
                uses.getOrPut(edgeKey(a, b)) { mutableListOf() } += ri
            }
        }

        val levels = plan.levelCount
        val corners = buildList {
            for (lv in 0 until levels) for (n in nodes) {
                add(iso(n.x, n.y, lv * STOREY_CM))
                add(iso(n.x, n.y, lv * STOREY_CM + WALL_H_CM))
            }
        }
        val minX = corners.minOf { it.x }; val maxX = corners.maxOf { it.x }
        val minY = corners.minOf { it.y }; val maxY = corners.maxOf { it.y }
        val pad = size.minDimension * 0.07f
        val scale = minOf(
            (size.width - pad * 2) / (maxX - minX).coerceAtLeast(1f),
            (size.height - pad * 2) / (maxY - minY).coerceAtLeast(1f),
        )
        val offX = (size.width - (maxX - minX) * scale) / 2f - minX * scale
        val offY = (size.height - (maxY - minY) * scale) / 2f - minY * scale
        fun p(x: Float, y: Float, z: Float): Offset {
            val o = iso(x, y, z)
            return Offset(offX + o.x * scale, offY + o.y * scale)
        }

        for (lv in 0 until levels) {
            val base = lv * STOREY_CM
            val roomsHere = plan.roomsOnLevel(lv)
            if (roomsHere.isEmpty()) continue

            roomsHere.forEach { ri ->
                drawPath(quad(plan.rooms[ri].map { p(nodes[it].x, nodes[it].y, base) }), floorFill)
            }

            // The open sides get a slab edge. Together with the wall tops it settles which way the
            // drawing reads: without them the eye flips the corner inside out (a Necker cube).
            val lips = mutableListOf<Pair<Float, DrawScope.() -> Unit>>()

            // Walls and furniture share one back-to-front pass, so a sofa sits in front of the wall
            // behind it and behind the one in front.
            val items = mutableListOf<Pair<Float, DrawScope.() -> Unit>>()

            roomsHere.forEach { ri ->
                val room = plan.rooms[ri]
                val cx = room.map { nodes[it].x }.average().toFloat()
                val cy = room.map { nodes[it].y }.average().toFloat()
                room.indices.forEach { i ->
                    val ai = room[i]; val bi = room[(i + 1) % room.size]
                    val exterior = uses[edgeKey(ai, bi)]?.size == 1
                    if (!exterior && ai > bi) return@forEach       // shared edge: draw it once
                    val a = nodes[ai]; val b = nodes[bi]
                    val mx = (a.x + b.x) / 2f; val my = (a.y + b.y) / 2f
                    // Outward normal, i.e. the one pointing away from the room. An exterior wall
                    // whose normal leans towards the viewer stands between them and the interior.
                    var nx = b.y - a.y; var ny = a.x - b.x
                    if (nx * (mx - cx) + ny * (my - cy) < 0f) { nx = -nx; ny = -ny }
                    val len = hypot(b.x - a.x, b.y - a.y).coerceAtLeast(1f)
                    nx /= len; ny /= len
                    if (exterior && nx + ny > 0f) {
                        lips += (mx + my) to {
                            drawPath(
                                quad(listOf(
                                    p(a.x, a.y, base), p(b.x, b.y, base),
                                    p(b.x, b.y, base - SLAB_T_CM), p(a.x, a.y, base - SLAB_T_CM),
                                )),
                                slabTone,
                            )
                        }
                        return@forEach
                    }
                    val tone = if (kotlin.math.abs(b.x - a.x) >= kotlin.math.abs(b.y - a.y)) faceTone else sideTone
                    items += (mx + my) to {
                        wall(plan, ai, bi, a, b, lv, base, nx, ny, ::p, tone, topTone, doorTone, windowTone)
                    }
                }
            }

            furniture.filter { it.level == lv }.forEach { f ->
                val w = (if (f.customWidthCm > 0f) f.customWidthCm else 60f) / 2f
                val d = (if (f.customDepthCm > 0f) f.customDepthCm else 60f) / 2f
                items += (f.posX + f.posZ) to { box(f.posX, f.posZ, w, d, base, FURN_H_CM, ::p, furnTone) }
            }

            lips.sortBy { it.first }
            lips.forEach { it.second(this) }
            items.sortBy { it.first }
            items.forEach { it.second(this) }
        }
    }
}

private fun edgeKey(a: Int, b: Int): Long = minOf(a, b).toLong() * 100_000L + maxOf(a, b)

private fun quad(points: List<Offset>): Path = Path().apply {
    points.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) }
    close()
}

/** One wall, split around its openings; the opening itself is a coloured panel at door or sill height. */
private fun DrawScope.wall(
    plan: FloorPlan, ai: Int, bi: Int, a: WallPoint, b: WallPoint,
    level: Int, base: Float, nx: Float, ny: Float, p: (Float, Float, Float) -> Offset,
    tone: Color, topTone: Color, doorTone: Color, windowTone: Color,
) {
    val len = hypot(b.x - a.x, b.y - a.y).coerceAtLeast(1f)
    val cuts = plan.openingsOn(ai, bi, level).map {
        val half = it.widthCm / 2f / len
        Triple((it.t - half).coerceIn(0f, 1f), (it.t + half).coerceIn(0f, 1f), it.type)
    }
    fun at(t: Float) = WallPoint(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
    fun panel(t0: Float, t1: Float, z0: Float, z1: Float, color: Color) {
        val s = at(t0); val e = at(t1)
        drawPath(
            quad(listOf(p(s.x, s.y, z0), p(e.x, e.y, z0), p(e.x, e.y, z1), p(s.x, s.y, z1))),
            color,
        )
    }

    var segs = listOf(0f to 1f)
    cuts.forEach { (c0, c1, _) ->
        segs = segs.flatMap { (s0, s1) ->
            when {
                c1 <= s0 || c0 >= s1 -> listOf(s0 to s1)
                else -> listOfNotNull(
                    (s0 to c0).takeIf { s0 < c0 },
                    (c1 to s1).takeIf { c1 < s1 },
                )
            }
        }
    }
    val top = base + WALL_H_CM
    segs.forEach { (s0, s1) -> panel(s0, s1, base, top, tone) }

    cuts.forEach { (c0, c1, type) ->
        val z0 = if (type == OpeningType.DOOR) base else base + WALL_H_CM * WINDOW_BASE
        val z1 = base + WALL_H_CM * (if (type == OpeningType.DOOR) DOOR_TOP else WINDOW_TOP)
        if (z0 > base) panel(c0, c1, base, z0, tone)
        if (z1 < top) panel(c0, c1, z1, top, tone)
        panel(c0, c1, z0, z1, if (type == OpeningType.DOOR) doorTone else windowTone)
    }

    // The lit top of the wall. Seeing it means you are looking down onto something standing up,
    // which is the cue that stops the corner reading as a solid block pointing at you.
    val ox = nx * WALL_T_CM; val oy = ny * WALL_T_CM
    drawPath(
        quad(listOf(
            p(a.x, a.y, top), p(b.x, b.y, top),
            p(b.x + ox, b.y + oy, top), p(a.x + ox, a.y + oy, top),
        )),
        topTone,
    )
}

/** An upright box for one piece of furniture: two sides in shadow, a lit top. */
private fun DrawScope.box(
    cx: Float, cy: Float, halfW: Float, halfD: Float,
    base: Float, height: Float, p: (Float, Float, Float) -> Offset, tone: Color,
) {
    val x0 = cx - halfW; val x1 = cx + halfW
    val y0 = cy - halfD; val y1 = cy + halfD
    val z = base + height
    drawPath(quad(listOf(p(x1, y0, base), p(x1, y1, base), p(x1, y1, z), p(x1, y0, z))), tone.copy(alpha = 0.80f))
    drawPath(quad(listOf(p(x0, y1, base), p(x1, y1, base), p(x1, y1, z), p(x0, y1, z))), tone.copy(alpha = 0.62f))
    drawPath(quad(listOf(p(x0, y0, z), p(x1, y0, z), p(x1, y1, z), p(x0, y1, z))), tone)
}
