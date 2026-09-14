package com.interiordesign3d.ui.screen.designer

import com.interiordesign3d.data.models.WallPoint

// ─── Editor / View state enums ────────────────────────────────────────────────

enum class EditorMode { DRAW_WALLS, DESIGN }

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
