package com.interiordesign3d.ui.screen.designer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.interiordesign3d.data.models.WallPoint
import java.util.UUID

// ─── Editor / View state enums ────────────────────────────────────────────────

enum class EditorMode { DRAW_WALLS, DESIGN }

enum class ViewMode(val label: String, val icon: ImageVector) {
    TOP_DOWN("Top View", Icons.Outlined.GridOn),
    PERSPECTIVE("3D View", Icons.Outlined.ViewInAr),
    ISOMETRIC("Isometric", Icons.Outlined.Layers)
}

/**
 * PLACING  — user is tapping to add wall corners
 * CLOSED   — current polygon just closed; "Done" button visible
 * EDITING  — floor plan committed; hold-drag moves corners, tap starts new room from a corner
 */
enum class DrawingPhase { PLACING, CLOSED, EDITING }

enum class PlacementTool { NONE, DOOR, WINDOW }

enum class OpeningHitZone { CENTER, END }

// ─── Hit-test / geometry helpers ─────────────────────────────────────────────

data class OpeningHit(
    val id: String,
    val aNode: WallPoint,
    val bNode: WallPoint,
    val zone: OpeningHitZone,
    val edgeLenCm: Float
)

data class WallInfo(
    val snappedX: Float, val snappedZ: Float,
    val normalX: Float,  val normalZ: Float,   // inward (toward room center)
    val tangentX: Float, val tangentZ: Float   // along wall direction
)

// ─── Room colors ──────────────────────────────────────────────────────────────

val ROOM_PALETTES = listOf(
    Color(0xFFC4A882) to Color(0xFFF5F0EB),
    Color(0xFF90CAF9) to Color(0xFFE3F2FD),
    Color(0xFFA5D6A7) to Color(0xFFE8F5E9),
    Color(0xFFFFCC80) to Color(0xFFFFF3E0),
    Color(0xFFCE93D8) to Color(0xFFF3E5F5),
    Color(0xFFEF9A9A) to Color(0xFFFFEBEE),
)

// ─── 3D renderer shape ────────────────────────────────────────────────────────

data class RoomShape(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val wallPoints: List<WallPoint> = emptyList(),
    val isClosed: Boolean = false,
    val floorColor: Color = Color(0xFFC4A882),
    val wallColor: Color = Color(0xFFF5F0EB)
)
