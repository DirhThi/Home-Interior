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
    val t: Float,              // position along nodeA → nodeB [0, 1]
    val type: OpeningType,
    val widthCm: Float = 90f, // 90 cm door, 120 cm window default
    val style: String = "",   // door leaf model key ("doorway" = cased opening, no leaf); "" = default leaf
    val leafHidden: Boolean = false,   // show the hole, not the door
    val leafOpen: Boolean = false,     // render the leaf swung open
)

@Serializable
data class FloorPlan(
    val nodes: List<WallPoint> = emptyList(),
    val rooms: List<List<Int>> = emptyList(),
    val openings: List<WallOpening> = emptyList()
) {
    fun roomPolygon(idx: Int): List<WallPoint> = rooms[idx].map { nodes[it] }

    /** Openings on the wall between two nodes, whichever order they were stored in. */
    fun openingsOn(a: Int, b: Int): List<WallOpening> = openings.filter {
        (it.nodeA == a && it.nodeB == b) || (it.nodeA == b && it.nodeB == a)
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
    val customHeightCm: Float = 0f
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
