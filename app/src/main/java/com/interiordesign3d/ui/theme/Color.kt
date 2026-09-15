package com.interiordesign3d.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Clay on white ─────────────────────────────────────────────────────────────

object InteriorColors {
    val Moss10 = Color(0xFF3A1206)
    val Moss20 = Color(0xFF571C08)
    val Moss30 = Color(0xFF7A2C12)
    val Moss40 = Color(0xFFB4472A)
    val Moss80 = Color(0xFFFFB49A)
    val Moss90 = Color(0xFFFFD5C6)
    val Moss95 = Color(0xFFFFE6DE)

    val Stone10 = Color(0xFF1A1A18)
    val Stone20 = Color(0xFF2A2A27)
    val Stone30 = Color(0xFF42423E)
    val Stone40 = Color(0xFF6E6E68)
    val Stone80 = Color(0xFFC7C7C2)
    val Stone90 = Color(0xFFE4E4E0)

    val Copper10 = Color(0xFF00201F)
    val Copper20 = Color(0xFF003735)
    val Copper30 = Color(0xFF00504D)
    val Copper40 = Color(0xFF1F6F6B)
    val Copper60 = Color(0xFF2E8F8A)
    val Copper80 = Color(0xFF7BD1CB)
    val Copper90 = Color(0xFFB8EFEA)

    val BoneGround = Color(0xFFFFFFFF)
    val Bone99 = Color(0xFFFFFFFF)
    val Bone97 = Color(0xFFF7F7F5)
    val Bone94 = Color(0xFFF1F1EE)
    val Bone90 = Color(0xFFE7E7E3)

    val Ink4  = Color(0xFF0A0A0A)
    val Ink8  = Color(0xFF111111)
    val Ink12 = Color(0xFF181818)
    val Ink14 = Color(0xFF1E1E1E)
    val Ink18 = Color(0xFF272727)
    val Ink22 = Color(0xFF323232)
    val Ink24 = Color(0xFF2C2C2C)

    val OnBone = Color(0xFF15151A)
    val OnInk  = Color(0xFFEDEDEB)

    val NeutralVar30 = Color(0xFF46464A)
    val NeutralVar80 = Color(0xFFC9C9C6)
    val NeutralVar90 = Color(0xFFE9E9E6)
    val Outline      = Color(0xFF78787C)
    val OutlineDark  = Color(0xFF909090)

    val Error10 = Color(0xFF410E0B)
    val Error20 = Color(0xFF601410)
    val Error30 = Color(0xFF8C1D18)
    val Error40 = Color(0xFFB3261E)
    val Error80 = Color(0xFFF2B8B5)
    val Error90 = Color(0xFFF9DEDC)
}

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
    // Home thumbnail: an axonometric doll's house needs its own opaque shades, because stacking
    // translucent walls turns the drawing into an x-ray at 64 dp.
    val thumbFloor: Color,
    val thumbWallFace: Color,
    val thumbWallSide: Color,
    val thumbWallTop: Color,
    val thumbSlab: Color,
    val thumbDoor: Color,
    val thumbFurniture: Color,
)

val LightAccents = InteriorAccents(
    door             = Color(0xFF1F6F6B),
    window           = Color(0xFF1F6F6B),
    canvasBackground = Color(0xFFFFFFFF),
    canvasGrid       = Color(0xFFEDEDEA),
    canvasGridMajor  = Color(0xFFD6D6D2),
    canvasWall       = InteriorColors.OnBone,
    canvasRoomFill   = Color(0x26B4472A),
    canvasNodeActive = InteriorColors.Moss40,
    canvasNodeStart  = InteriorColors.Copper60,
    canvasNodeIdle   = Color(0xFFA8A8A4),
    canvasFurniture  = InteriorColors.Moss40,
    dimensionText      = InteriorColors.Copper40,
    dimensionChip      = Color(0xEBFFFFFF),
    viewportBackground = Color(0xFF2A211D),
    thumbFloor         = Color(0xFFEFE2DB),
    thumbWallFace      = Color(0xFFDCD6D1),
    thumbWallSide      = Color(0xFFBEB6AF),
    thumbWallTop       = Color(0xFFF3EFEB),
    thumbSlab          = Color(0xFFCFC2BA),
    thumbDoor          = InteriorColors.Moss40,
    thumbFurniture     = Color(0xFF8C7A6B),
)

val DarkAccents = InteriorAccents(
    door             = Color(0xFF7BD1CB),
    window           = Color(0xFF7BD1CB),
    canvasBackground = Color(0xFF0C0C0C),
    canvasGrid       = Color(0xFF1C1C1C),
    canvasGridMajor  = Color(0xFF2E2E2E),
    canvasWall       = InteriorColors.OnInk,
    canvasRoomFill   = Color(0x33FFB49A),
    canvasNodeActive = InteriorColors.Moss80,
    canvasNodeStart  = InteriorColors.Copper80,
    canvasNodeIdle   = Color(0xFF6C6C6C),
    canvasFurniture  = InteriorColors.Moss80,
    dimensionText      = InteriorColors.Copper80,
    dimensionChip      = Color(0xE60C0C0C),
    viewportBackground = Color(0xFF140F0D),
    thumbFloor         = Color(0xFF3A302B),
    thumbWallFace      = Color(0xFF4A423C),
    thumbWallSide      = Color(0xFF332D29),
    thumbWallTop       = Color(0xFF5E544C),
    thumbSlab          = Color(0xFF2A221E),
    thumbDoor          = InteriorColors.Moss80,
    thumbFurniture     = Color(0xFFA8907E),
)

val LocalInteriorAccents = staticCompositionLocalOf { LightAccents }
