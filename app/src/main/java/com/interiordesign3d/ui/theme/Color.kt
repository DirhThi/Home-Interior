package com.interiordesign3d.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Moss & Bone ramps ────────────────────────────────────────────────────────

object InteriorColors {
    // Moss — the primary
    val Moss10 = Color(0xFF0B2019)
    val Moss20 = Color(0xFF0B3427)
    val Moss30 = Color(0xFF1C4A3C)
    val Moss40 = Color(0xFF2F5D50)
    val Moss80 = Color(0xFF86C7B0)
    val Moss90 = Color(0xFFA2E3CB)
    val Moss95 = Color(0xFFCCE8DC)

    // Stone — the quiet secondary
    val Stone10 = Color(0xFF171D1A)
    val Stone20 = Color(0xFF21332A)
    val Stone30 = Color(0xFF374A40)
    val Stone40 = Color(0xFF5A6B62)
    val Stone80 = Color(0xFFB6C6BC)
    val Stone90 = Color(0xFFDDE6E0)

    // Copper — the accent
    val Copper10 = Color(0xFF351A06)
    val Copper20 = Color(0xFF4A2409)
    val Copper30 = Color(0xFF683A17)
    val Copper40 = Color(0xFFA15A2C)
    val Copper60 = Color(0xFFC97B4A)
    val Copper80 = Color(0xFFE09A6B)
    val Copper90 = Color(0xFFFFDCC6)

    // Bone — surfaces
    val BoneGround = Color(0xFFE9E5DA)   // the ground everything else sits on
    val Bone99 = Color(0xFFFAF8F3)
    val Bone97 = Color(0xFFF4F1EA)
    val Bone94 = Color(0xFFEDE9DF)
    val Bone90 = Color(0xFFE3DED1)

    // Ink — dark surfaces
    val Ink4  = Color(0xFF070D0A)
    val Ink8  = Color(0xFF0D1411)
    val Ink12 = Color(0xFF16201C)
    val Ink14 = Color(0xFF1A2521)
    val Ink18 = Color(0xFF24302B)
    val Ink22 = Color(0xFF2E3A35)
    val Ink24 = Color(0xFF2A302C)

    val OnBone = Color(0xFF14201C)
    val OnInk  = Color(0xFFE8EDE9)

    val NeutralVar30 = Color(0xFF434A45)
    val NeutralVar80 = Color(0xFFC3C9C0)
    val NeutralVar90 = Color(0xFFDFE5DF)
    val Outline      = Color(0xFF73796F)
    val OutlineDark  = Color(0xFF8D938A)

    val Error10 = Color(0xFF410E0B)
    val Error20 = Color(0xFF601410)
    val Error30 = Color(0xFF8C1D18)
    val Error40 = Color(0xFFB3261E)
    val Error80 = Color(0xFFF2B8B5)
    val Error90 = Color(0xFFF9DEDC)
}

// ─── Colours the Material scheme has no slot for ──────────────────────────────

/** Openings and the 2D floor-plan canvas, which paints outside the Material palette. */
@Immutable
data class InteriorAccents(
    val door: Color,
    val window: Color,
    val canvasBackground: Color,
    val canvasGrid: Color,
    val canvasGridMajor: Color,
    val canvasWall: Color,
    val canvasRoomFill: Color,
    val canvasNodeActive: Color,
    val canvasNodeStart: Color,
    val canvasNodeIdle: Color,
    val canvasFurniture: Color,
    val dimensionText: Color,
    val dimensionChip: Color,
    val viewportBackground: Color,
)

val LightAccents = InteriorAccents(
    door             = InteriorColors.Copper40,
    window           = InteriorColors.Moss40,
    canvasBackground = Color(0xFFFBF9F4),
    canvasGrid       = Color(0xFFDCE0D4),
    canvasGridMajor  = Color(0xFFBFC7B6),
    canvasWall       = InteriorColors.OnBone,
    canvasRoomFill   = Color(0x262F5D50),
    canvasNodeActive = InteriorColors.Moss40,
    canvasNodeStart  = InteriorColors.Copper60,
    canvasNodeIdle   = Color(0xFF9AA79E),
    canvasFurniture  = InteriorColors.Moss40,
    dimensionText      = InteriorColors.Copper40,
    dimensionChip      = Color(0xEBFBF9F4),
    viewportBackground = Color(0xFF5E655C),
)

val DarkAccents = InteriorAccents(
    door             = InteriorColors.Copper80,
    window           = InteriorColors.Moss80,
    canvasBackground = Color(0xFF0A100D),
    canvasGrid       = Color(0xFF1D2823),
    canvasGridMajor  = Color(0xFF32413A),
    canvasWall       = InteriorColors.OnInk,
    canvasRoomFill   = Color(0x3386C7B0),
    canvasNodeActive = InteriorColors.Moss80,
    canvasNodeStart  = InteriorColors.Copper80,
    canvasNodeIdle   = Color(0xFF6E7A73),
    canvasFurniture  = InteriorColors.Moss80,
    dimensionText      = InteriorColors.Copper80,
    dimensionChip      = Color(0xE60A100D),
    viewportBackground = Color(0xFF121915),
)

val LocalInteriorAccents = staticCompositionLocalOf { LightAccents }
