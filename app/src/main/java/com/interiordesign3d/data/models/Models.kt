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

@Serializable
data class FloorPlan(
    // Nodes are shared across storeys on purpose: upper walls land on lower ones, and the storey
    // below can be traced directly when drawing the one above.
    val nodes: List<WallPoint> = emptyList(),
    val rooms: List<List<Int>> = emptyList(),
    val openings: List<WallOpening> = emptyList(),
    /** Parallel to [rooms]; an empty list means every room is on the ground floor. */
    val roomLevels: List<Int> = emptyList()
) {
    fun roomPolygon(idx: Int): List<WallPoint> = rooms[idx].map { nodes[it] }

    fun levelOf(roomIdx: Int): Int = roomLevels.getOrElse(roomIdx) { 0 }

    val levelCount: Int get() = (roomLevels.maxOrNull() ?: 0) + 1

    /** Indices into [rooms] for one storey. */
    fun roomsOnLevel(level: Int): List<Int> = rooms.indices.filter { levelOf(it) == level }

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
