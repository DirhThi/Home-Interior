package com.interiordesign3d.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

// ─── Furniture Item ────────────────────────────────────────────────────────────

@Parcelize
@Serializable
data class FurnitureItem(
    val id: String,
    val name: String,
    val category: FurnitureCategory,
    val brand: String,
    val price: Double,
    val thumbnailUrl: String,
    val modelUrl: String,
    val widthCm: Float,
    val depthCm: Float,
    val heightCm: Float,
    val availableColors: List<String>,
    val description: String,
    val tags: List<String> = emptyList()
) : Parcelable

enum class FurnitureCategory(val displayName: String, val icon: String) {
    SOFA("Sofas", "🛋️"),
    CHAIR("Chairs", "🪑"),
    TABLE("Tables", "🪞"),
    BED("Beds", "🛏️"),
    WARDROBE("Wardrobes", "🚪"),
    BOOKSHELF("Bookshelves", "📚"),
    LAMP("Lamps", "💡"),
    RUG("Rugs", "🟫"),
    PLANT("Plants", "🪴"),
    DECOR("Décor", "🎨")
}

// ─── Wall Point & Floor Plan ──────────────────────────────────────────────────

@Serializable
data class WallPoint(val x: Float, val y: Float)

@Serializable
enum class OpeningType { DOOR, WINDOW }

@Serializable
data class WallOpening(
    val id: String = "",
    // The wall this opening cuts, as the pair of plan nodes it runs between. Addressing it this way
    // rather than by (room, edge index) is what lets ONE opening cut a wall two rooms share.
    val nodeA: Int = -1,
    val nodeB: Int = -1,
    val level: Int = 0,        // storey this wall belongs to
    val t: Float,              // position along nodeA → nodeB [0, 1]
    val type: OpeningType,
    val widthCm: Float = 90f, // 90 cm door, 120 cm window default
    val style: String = "",   // door leaf model key ("doorway" = cased opening, no leaf); "" = default leaf
    val leafHidden: Boolean = false,   // show the hole, not the door
    val leafOpen: Boolean = false,     // render the leaf swung open
)

@Serializable
enum class StairShape { STRAIGHT, L_SHAPED, U_SHAPED }

/**
 * A flight rising from [level] to the storey above. Every shape is described by one centre-line
 * polyline plus a run width, so steps, footprint and floor opening all come from the same numbers.
 * Height is never a parameter — a flight always spans exactly one storey.
 */
@Serializable
data class Stair(
    val id: String = "",
    val level: Int = 0,
    val x: Float = 0f,          // centre of the bounding box, cm
    val y: Float = 0f,
    val shape: StairShape = StairShape.STRAIGHT,
    val widthCm: Float = 100f,  // width of one run
    val lengthCm: Float = 400f, // box length, along the first run
    val legCm: Float = 200f,    // L only: the second run
    val wellCm: Float = 20f,    // U only: the gap between the two runs
    val rotationDeg: Float = 0f,
) {
    /** Size of the footprint's bounding box, before rotation. */
    val boxWidth: Float get() = when (shape) {
        StairShape.STRAIGHT -> widthCm
        StairShape.L_SHAPED -> widthCm + legCm
        StairShape.U_SHAPED -> widthCm * 2f + wellCm
    }
    val boxLength: Float get() = lengthCm

    /**
     * The flight broken into straight runs and the flat landings between them. Steps belong to the
     * runs only — railing them continuously through a turn left the corner steps fanned out with
     * gaps between them instead of a platform.
     */
    fun runs(): List<Pair<WallPoint, WallPoint>> =
        localRuns().map { (a, b) -> toPlanFromBox(a) to toPlanFromBox(b) }

    private fun localRuns(): List<Pair<WallPoint, WallPoint>> {
        val w = widthCm
        val half = w / 2f
        val l = lengthCm
        return when (shape) {
            StairShape.STRAIGHT -> listOf(WallPoint(half, 0f) to WallPoint(half, l))
            StairShape.L_SHAPED -> listOf(
                WallPoint(half, 0f) to WallPoint(half, l - w),
                WallPoint(w, l - half) to WallPoint(w + legCm, l - half),
            )
            StairShape.U_SHAPED -> listOf(
                WallPoint(half, 0f) to WallPoint(half, l - w),
                WallPoint(w + wellCm + half, l - w) to WallPoint(w + wellCm + half, 0f),
            )
        }
    }

    /** Flat platforms where runs meet: centre in plan, plus size along the box's own axes. */
    fun landings(): List<Triple<WallPoint, Float, Float>> =
        localLandings().map { (c, su, sv) -> Triple(toPlanFromBox(c), su, sv) }

    private fun localLandings(): List<Triple<WallPoint, Float, Float>> {
        val w = widthCm
        val l = lengthCm
        return when (shape) {
            StairShape.STRAIGHT -> emptyList()
            StairShape.L_SHAPED -> listOf(Triple(WallPoint(w / 2f, l - w / 2f), w, w))
            StairShape.U_SHAPED -> listOf(Triple(WallPoint(boxWidth / 2f, l - w / 2f), boxWidth, w))
        }
    }

    /**
     * Per landing, the edges no run arrives at — the open sides, which is exactly where a guard rail
     * belongs. Deriving it beats hard-coding per shape: the L and U landings differ in which sides
     * are free, and a new shape would get its rails for nothing.
     */
    fun landingRails(): List<List<Pair<WallPoint, WallPoint>>> {
        val ends = localRuns().flatMap { listOf(it.first, it.second) }
        return localLandings().map { (c, su, sv) ->
            val x0 = c.x - su / 2f; val x1 = c.x + su / 2f
            val y0 = c.y - sv / 2f; val y1 = c.y + sv / 2f
            listOf(
                WallPoint(x0, y0) to WallPoint(x1, y0),
                WallPoint(x1, y0) to WallPoint(x1, y1),
                WallPoint(x1, y1) to WallPoint(x0, y1),
                WallPoint(x0, y1) to WallPoint(x0, y0),
            ).filter { (a, b) -> ends.none { touchesSegment(it, a, b) } }
                .map { (a, b) -> toPlanFromBox(a) to toPlanFromBox(b) }
        }
    }

    private fun touchesSegment(p: WallPoint, a: WallPoint, b: WallPoint): Boolean {
        val dx = b.x - a.x; val dy = b.y - a.y
        val len2 = (dx * dx + dy * dy).coerceAtLeast(1e-3f)
        val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / len2).coerceIn(0f, 1f)
        return kotlin.math.hypot(a.x + dx * t - p.x, a.y + dy * t - p.y) < 1f
    }

    private fun toPlanFromBox(p: WallPoint) =
        toPlan(WallPoint(p.x - boxWidth / 2f, p.y - boxLength / 2f))

    /** Centre line in bounding-box coordinates, origin at the box's bottom-left corner. */
    fun centreLine(): List<WallPoint> {
        val w = widthCm
        val half = w / 2f
        return when (shape) {
            StairShape.STRAIGHT -> listOf(WallPoint(half, 0f), WallPoint(half, lengthCm))
            StairShape.L_SHAPED -> listOf(
                WallPoint(half, 0f),
                WallPoint(half, lengthCm - half),
                WallPoint(w + legCm, lengthCm - half),
            )
            StairShape.U_SHAPED -> listOf(
                WallPoint(half, 0f),
                WallPoint(half, lengthCm - half),
                WallPoint(w + wellCm + half, lengthCm - half),
                WallPoint(w + wellCm + half, 0f),
            )
        }
    }

    /** Outline in bounding-box coordinates, counter-clockwise. */
    private fun outline(): List<WallPoint> {
        val w = widthCm
        val l = lengthCm
        return when (shape) {
            StairShape.STRAIGHT -> listOf(
                WallPoint(0f, 0f), WallPoint(w, 0f), WallPoint(w, l), WallPoint(0f, l),
            )
            StairShape.L_SHAPED -> listOf(
                WallPoint(0f, 0f), WallPoint(w, 0f), WallPoint(w, l - w),
                WallPoint(w + legCm, l - w), WallPoint(w + legCm, l), WallPoint(0f, l),
            )
            StairShape.U_SHAPED -> listOf(
                WallPoint(0f, 0f), WallPoint(w, 0f), WallPoint(w, l - w),
                WallPoint(w + wellCm, l - w), WallPoint(w + wellCm, 0f),
                WallPoint(w * 2f + wellCm, 0f), WallPoint(w * 2f + wellCm, l), WallPoint(0f, l),
            )
        }
    }

    /**
     * The stairwell: the rectangle cut out of the slab above. A real opening is rectangular even
     * when the flight turns, and a rectangle always triangulates — the L and U outlines are concave
     * and made the ear clipper give up, which showed as a ragged hole.
     */
    fun wellOpening(marginCm: Float = 0f): List<WallPoint> {
        val hw = boxWidth / 2f + marginCm
        val hl = boxLength / 2f + marginCm
        return listOf(-hw to -hl, hw to -hl, hw to hl, -hw to hl)
            .map { (u, v) -> toPlan(WallPoint(u, v)) }
    }

    /** The footprint in plan centimetres; [marginCm] grows it so the opening is not pinched. */
    fun footprint(marginCm: Float = 0f): List<WallPoint> {
        val bw = boxWidth
        val bl = boxLength
        val sx = if (bw > 1f) (bw + marginCm * 2f) / bw else 1f
        val sy = if (bl > 1f) (bl + marginCm * 2f) / bl else 1f
        return outline().map { toPlan(WallPoint((it.x - bw / 2f) * sx, (it.y - bl / 2f) * sy)) }
    }

    /** Bounding-box coordinates → plan coordinates. */
    fun toPlan(local: WallPoint): WallPoint {
        val r = Math.toRadians(rotationDeg.toDouble())
        val c = kotlin.math.cos(r).toFloat()
        val s = kotlin.math.sin(r).toFloat()
        return WallPoint(x + local.x * c - local.y * s, y + local.x * s + local.y * c)
    }

    /** Total distance walked, i.e. the sum of the runs. Landings are not walked up. */
    fun goingCm(): Float = runs().sumOf { (a, b) ->
        kotlin.math.hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble())
    }.toFloat()

    /** Depth of one step for a storey rising [riseCm]. Below [MIN_COMFORTABLE_TREAD_CM] it is a ladder. */
    fun treadCm(riseCm: Float): Float = goingCm() / stepCount(riseCm)

    companion object {
        private const val TARGET_RISER_CM = 17f

        /** One flight spans exactly one storey, so the step count follows from the rise alone. */
        fun stepCount(riseCm: Float): Int =
            (riseCm / TARGET_RISER_CM).roundToInt().coerceIn(10, 28)

        /**
         * Box length that leaves room for a comfortable tread at a normal storey height. It differs
         * per shape because a turning flight gets a second run for free: a U walks 2 × (length −
         * width), so it needs far less depth than a straight flight covering the same going.
         */
        fun defaultLengthCm(shape: StairShape): Float = when (shape) {
            StairShape.STRAIGHT -> 400f
            StairShape.L_SHAPED -> 300f
            StairShape.U_SHAPED -> 300f
        }
    }
}

/** Wall and floor finish for one storey. Lives in the plan because it is per-storey data. */
@Serializable
data class LevelSurface(
    val wallPresetIdx: Int = 0,
    val floorPresetIdx: Int = 0,
    val wallColor: String = "",   // blank = take the tint from the wall preset
)

@Serializable
data class FloorPlan(
    // Nodes are shared across storeys on purpose: upper walls land on lower ones, and the storey
    // below can be traced directly when drawing the one above.
    val nodes: List<WallPoint> = emptyList(),
    val rooms: List<List<Int>> = emptyList(),
    val openings: List<WallOpening> = emptyList(),
    /** Parallel to [rooms]; an empty list means every room is on the ground floor. */
    val roomLevels: List<Int> = emptyList(),
    val stairs: List<Stair> = emptyList(),
    val levelSurfaces: List<LevelSurface> = emptyList()
) {
    fun roomPolygon(idx: Int): List<WallPoint> = rooms[idx].map { nodes[it] }

    fun levelOf(roomIdx: Int): Int = roomLevels.getOrElse(roomIdx) { 0 }

    fun surfaceOf(level: Int): LevelSurface = levelSurfaces.getOrElse(level) { LevelSurface() }

    fun withSurface(level: Int, transform: (LevelSurface) -> LevelSurface): FloorPlan {
        val list = levelSurfaces.toMutableList()
        while (list.size <= level) list += LevelSurface()
        list[level] = transform(list[level])
        return copy(levelSurfaces = list)
    }

    val levelCount: Int get() = (roomLevels.maxOrNull() ?: 0) + 1

    /** Indices into [rooms] for one storey. */
    fun roomsOnLevel(level: Int): List<Int> = rooms.indices.filter { levelOf(it) == level }

    /** Holes that the floor of [level] must carry: the stairs coming up from the storey below. */
    fun floorHoles(level: Int): List<List<WallPoint>> =
        stairs.filter { it.level == level - 1 }.map { it.wellOpening(HOLE_MARGIN_CM) }

    /**
     * Whether [stair] can actually open onto the storey above. The hole is cut out of ONE room's
     * slab, so the whole footprint has to sit inside one room up there — a flight straddling a wall,
     * hanging over the edge, or with nothing above it gets no opening.
     */
    /**
     * Shrinks and nudges [stair] until its whole footprint sits inside ONE room of the storey it
     * opens onto — the hole is cut from a single slab, so a flight spanning two rooms cannot work.
     * Width and length give way; the height is always the storey, so it is not ours to change.
     */
    fun fitStair(stair: Stair): Stair {
        val candidates = roomsOnLevel(stair.level + 1).ifEmpty { roomsOnLevel(stair.level) }
        val centre = WallPoint(stair.x, stair.y)
        val target = candidates.firstOrNull { pointInPolygon(centre, rooms[it].map { n -> nodes[n] }) }
            ?: candidates.firstOrNull() ?: return stair
        val poly = rooms[target].map { nodes[it] }

        // Work in the flight's own frame, where its bounding box is axis-aligned.
        val rad = Math.toRadians(-stair.rotationDeg.toDouble())
        val c = kotlin.math.cos(rad).toFloat()
        val sn = kotlin.math.sin(rad).toFloat()
        fun toLocal(p: WallPoint) = WallPoint(p.x * c - p.y * sn, p.x * sn + p.y * c)
        fun toWorld(p: WallPoint) = WallPoint(p.x * c + p.y * sn, -p.x * sn + p.y * c)

        val local = poly.map { toLocal(it) }
        val minX = local.minOf { it.x } + HOLE_MARGIN_CM
        val maxX = local.maxOf { it.x } - HOLE_MARGIN_CM
        val minY = local.minOf { it.y } + HOLE_MARGIN_CM
        val maxY = local.maxOf { it.y } - HOLE_MARGIN_CM
        val availW = maxX - minX
        val availL = maxY - minY
        if (availW < MIN_STAIR_WIDTH_CM || availL < MIN_STAIR_LENGTH_CM) return stair

        // Shrink every run together so the shape keeps its proportions.
        val k = minOf(availW / stair.boxWidth, availL / stair.boxLength, 1f)
        var fitted = if (k >= 1f) stair else stair.copy(
            widthCm = (stair.widthCm * k).coerceAtLeast(MIN_STAIR_WIDTH_CM),
            lengthCm = (stair.lengthCm * k).coerceAtLeast(MIN_STAIR_LENGTH_CM),
            legCm = stair.legCm * k,
            wellCm = stair.wellCm * k,
        )

        val hw = fitted.boxWidth / 2f
        val hl = fitted.boxLength / 2f
        val lc = toLocal(centre)
        val cx = if (availW < fitted.boxWidth) (minX + maxX) / 2f else lc.x.coerceIn(minX + hw, maxX - hw)
        val cy = if (availL < fitted.boxLength) (minY + maxY) / 2f else lc.y.coerceIn(minY + hl, maxY - hl)
        val world = toWorld(WallPoint(cx, cy))
        return fitted.copy(x = world.x, y = world.y)
    }

    fun stairFits(stair: Stair): Boolean {
        val above = roomsOnLevel(stair.level + 1)
        if (above.isEmpty()) return false
        val fp = stair.wellOpening(HOLE_MARGIN_CM)
        return above.any { ri ->
            val poly = rooms[ri].map { nodes[it] }
            fp.all { pointInPolygon(it, poly) }
        }
    }

    /** Appends a room on [level], keeping [roomLevels] aligned with [rooms]. */
    fun addRoom(polygon: List<Int>, level: Int): FloorPlan {
        val levels = roomLevels.toMutableList()
        while (levels.size < rooms.size) levels += 0
        levels += level
        return copy(rooms = rooms + listOf(polygon), roomLevels = levels)
    }

    fun dropLastRoom(): FloorPlan {
        if (rooms.isEmpty()) return this
        val levels = roomLevels.toMutableList()
        while (levels.size < rooms.size) levels += 0
        return copy(rooms = rooms.dropLast(1), roomLevels = levels.dropLast(1))
    }

    /** Openings on the wall between two nodes, whichever order they were stored in. */
    /**
     * The storey's outline, as ordered rings of plan points. Built from the edges exactly one room
     * uses: taken in that room's own winding they already point the same way round the outside, so
     * walking them needs no geometry beyond following the chain.
     *
     * More than one ring means the storey is in disconnected pieces; a ring wound the other way is
     * a courtyard. Both are things a user can draw, so callers get a list, not one polygon.
     */
    fun outlineRings(level: Int): List<List<WallPoint>> {
        val here = roomsOnLevel(level)
        if (here.isEmpty()) return emptyList()

        val shared = HashSet<Long>()
        val seen = HashSet<Long>()
        val rings = here.map { ri ->
            val ring = rooms[ri]
            if (signedArea2(ring.map { nodes[it] }) < 0f) ring.reversed() else ring
        }
        rings.forEach { ring ->
            ring.indices.forEach { i ->
                val k = edgeKey(ring[i], ring[(i + 1) % ring.size])
                if (!seen.add(k)) shared += k
            }
        }

        val next = HashMap<Int, MutableList<Int>>()
        rings.forEach { ring ->
            ring.indices.forEach { i ->
                val a = ring[i]; val b = ring[(i + 1) % ring.size]
                if (edgeKey(a, b) !in shared) next.getOrPut(a) { mutableListOf() } += b
            }
        }

        val out = mutableListOf<List<WallPoint>>()
        while (next.values.any { it.isNotEmpty() }) {
            val start = next.entries.first { it.value.isNotEmpty() }.key
            val path = mutableListOf(start)
            var cur = start
            var prev = -1
            while (true) {
                val outs = next[cur] ?: break
                if (outs.isEmpty()) break
                // A pinch point has several ways on; the sharpest left turn hugs this ring.
                val pick = if (outs.size == 1 || prev < 0) 0 else outs.indices.maxBy {
                    turn(nodes[prev], nodes[cur], nodes[outs[it]])
                }
                val nxt = outs.removeAt(pick)
                if (nxt == start) break
                if (nxt in path) break        // ran into itself: stop rather than loop forever
                path += nxt
                prev = cur
                cur = nxt
            }
            if (path.size >= 3) out += path.map { nodes[it] }
        }
        return out
    }

    private fun turn(a: WallPoint, b: WallPoint, c: WallPoint): Float {
        val ax = b.x - a.x; val ay = b.y - a.y
        val bx = c.x - b.x; val by = c.y - b.y
        return Math.atan2((ax * by - ay * bx).toDouble(), (ax * bx + ay * by).toDouble()).toFloat()
    }

    private fun edgeKey(a: Int, b: Int): Long = minOf(a, b).toLong() * 100_000L + maxOf(a, b)

    fun openingsOn(a: Int, b: Int, level: Int): List<WallOpening> = openings.filter {
        it.level == level && ((it.nodeA == a && it.nodeB == b) || (it.nodeA == b && it.nodeB == a))
    }

}

// ─── DesignRoom ───────────────────────────────────────────────────────────────

/**
 * The room record. It deliberately holds nothing that [FloorPlan] already knows: size, shape, room
 * count and every wall/floor finish live in [floorPlanJson]. Summary columns here went stale the
 * moment the plan took over, and the Home card spent a while showing the values they froze at.
 */
@Entity(tableName = "rooms")
@Parcelize
@Serializable
data class DesignRoom(
    @PrimaryKey val id: String,
    val name: String,
    val heightCm: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val stairPresetIdx: Int = 2,   // a different timber from the floor by default, so a flight reads
    val shadowsEnabled: Boolean = false,
    val autoHideWalls: Boolean = false,
    // JSON-encoded FloorPlan (multi-room with shared nodes)
    val floorPlanJson: String = ""
) : Parcelable

// ─── Placed Furniture (in a room) ─────────────────────────────────────────────

@Entity(tableName = "placed_furniture")
@Parcelize
@Serializable
data class PlacedFurniture(
    @PrimaryKey val id: String,
    val roomId: String,
    val furnitureId: String,
    val furnitureName: String,
    val modelUrl: String,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,
    val rotationY: Float = 0f,
    val scale: Float = 1.0f,
    val colorOverride: String? = null,
    val isWallMounted: Boolean = false,
    val wallMountHeight: Float = 120f,
    val customWidthCm: Float = 0f,
    val customDepthCm: Float = 0f,
    val customHeightCm: Float = 0f,
    val level: Int = 0
) : Parcelable

// ─── Color Palette (used by ColorPickerScreen) ────────────────────────────────

enum class DesignStyle(val displayName: String) {
    MINIMALIST("Minimalist"),
    SCANDINAVIAN("Scandinavian"),
    INDUSTRIAL("Industrial"),
    BOHEMIAN("Bohemian"),
    MODERN("Modern"),
    CLASSIC("Classic"),
    JAPANDI("Japandi"),
    COASTAL("Coastal"),
    MAXIMALIST("Maximalist"),
    MID_CENTURY("Mid-Century Modern")
}

data class ColorPalette(
    val id: String,
    val name: String,
    val primary: String,
    val secondary: String,
    val accent: String,
    val background: String,
    val style: DesignStyle
)

/** Stair holes are cut a little wider than the run so the flight is not pinched by the slab edge. */
const val HOLE_MARGIN_CM = 4f

fun pointInPolygon(pt: WallPoint, poly: List<WallPoint>): Boolean {
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val a = poly[i]; val b = poly[j]
        if ((a.y > pt.y) != (b.y > pt.y) &&
            pt.x < (b.x - a.x) * (pt.y - a.y) / (b.y - a.y) + a.x
        ) inside = !inside
        j = i
    }
    return inside
}

/** Twice the signed area; positive means counter-clockwise in plan coordinates. */
fun signedArea2(poly: List<WallPoint>): Float {
    var a = 0f
    for (i in poly.indices) {
        val p = poly[i]; val q = poly[(i + 1) % poly.size]
        a += p.x * q.y - q.x * p.y
    }
    return a
}

const val MIN_STAIR_WIDTH_CM = 70f
const val MIN_STAIR_LENGTH_CM = 150f
/** A storey sits on the slab of the one below, not on its wall tops. */
const val FLOOR_SLAB_CM = 5f
/** Shallower than this and the flight reads as a ladder; the panel says so rather than blocking it. */
const val MIN_COMFORTABLE_TREAD_CM = 22f
