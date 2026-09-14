package com.interiordesign3d.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
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

/**
 * A flight rising from [level] to the storey above. Its footprint is the rectangle that gets cut
 * out of the floor overhead — place the stair and the hole follows from its own coordinates.
 */
@Serializable
data class Stair(
    val id: String = "",
    val level: Int = 0,
    val x: Float = 0f,          // centre of the run, cm
    val y: Float = 0f,
    val widthCm: Float = 100f,
    val lengthCm: Float = 240f,
    val rotationDeg: Float = 0f,
) {
    /** The four corners of the run, in plan centimetres. */
    fun footprint(marginCm: Float = 0f): List<WallPoint> {
        val hw = widthCm / 2f + marginCm
        val hl = lengthCm / 2f + marginCm
        val r = Math.toRadians(rotationDeg.toDouble())
        val c = kotlin.math.cos(r).toFloat()
        val s = kotlin.math.sin(r).toFloat()
        return listOf(-hw to -hl, hw to -hl, hw to hl, -hw to hl).map { (u, v) ->
            WallPoint(x + u * c - v * s, y + u * s + v * c)
        }
    }
}

@Serializable
data class FloorPlan(
    // Nodes are shared across storeys on purpose: upper walls land on lower ones, and the storey
    // below can be traced directly when drawing the one above.
    val nodes: List<WallPoint> = emptyList(),
    val rooms: List<List<Int>> = emptyList(),
    val openings: List<WallOpening> = emptyList(),
    /** Parallel to [rooms]; an empty list means every room is on the ground floor. */
    val roomLevels: List<Int> = emptyList(),
    val stairs: List<Stair> = emptyList()
) {
    fun roomPolygon(idx: Int): List<WallPoint> = rooms[idx].map { nodes[it] }

    fun levelOf(roomIdx: Int): Int = roomLevels.getOrElse(roomIdx) { 0 }

    val levelCount: Int get() = (roomLevels.maxOrNull() ?: 0) + 1

    /** Indices into [rooms] for one storey. */
    fun roomsOnLevel(level: Int): List<Int> = rooms.indices.filter { levelOf(it) == level }

    /** Holes that the floor of [level] must carry: the stairs coming up from the storey below. */
    fun floorHoles(level: Int): List<List<WallPoint>> =
        stairs.filter { it.level == level - 1 }.map { it.footprint(HOLE_MARGIN_CM) }

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

        // Work in the flight's own frame, where its footprint is axis-aligned.
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
        if (maxX - minX < MIN_STAIR_WIDTH_CM || maxY - minY < MIN_STAIR_LENGTH_CM) return stair

        val w = stair.widthCm.coerceIn(MIN_STAIR_WIDTH_CM, maxX - minX)
        val l = stair.lengthCm.coerceIn(MIN_STAIR_LENGTH_CM, maxY - minY)
        val lc = toLocal(centre)
        val fitted = toWorld(
            WallPoint(
                lc.x.coerceIn(minX + w / 2f, maxX - w / 2f),
                lc.y.coerceIn(minY + l / 2f, maxY - l / 2f),
            )
        )
        return stair.copy(x = fitted.x, y = fitted.y, widthCm = w, lengthCm = l)
    }

    fun stairFits(stair: Stair): Boolean {
        val above = roomsOnLevel(stair.level + 1)
        if (above.isEmpty()) return false
        val fp = stair.footprint(HOLE_MARGIN_CM)
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
    fun openingsOn(a: Int, b: Int, level: Int): List<WallOpening> = openings.filter {
        it.level == level && ((it.nodeA == a && it.nodeB == b) || (it.nodeA == b && it.nodeB == a))
    }

}

// ─── DesignRoom ───────────────────────────────────────────────────────────────

@Entity(tableName = "rooms")
@Parcelize
@Serializable
data class DesignRoom(
    @PrimaryKey val id: String,
    val name: String,
    val widthCm: Float,
    val lengthCm: Float,
    val heightCm: Float,
    val wallColor: String = "#F5F0EB",
    val floorMaterial: FloorMaterial = FloorMaterial.HARDWOOD,
    val floorColor: String = "#C4A882",
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val wallPointsJson: String = "",
    val wallPresetIdx: Int = 0,
    val floorPresetIdx: Int = 0,
    val shadowsEnabled: Boolean = false,
    val autoHideWalls: Boolean = false,
    // JSON-encoded FloorPlan (multi-room with shared nodes)
    val floorPlanJson: String = ""
) : Parcelable

enum class FloorMaterial(val displayName: String) {
    HARDWOOD("Hardwood"),
    MARBLE("Marble"),
    TILE("Tile"),
    CARPET("Carpet"),
    CONCRETE("Concrete"),
    LAMINATE("Laminate")
}

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

const val MIN_STAIR_WIDTH_CM = 70f
const val MIN_STAIR_LENGTH_CM = 150f
