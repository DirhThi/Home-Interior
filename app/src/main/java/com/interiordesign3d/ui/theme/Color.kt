package com.interiordesign3d.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Brand ramps ──────────────────────────────────────────────────────────────

object InteriorColors {
    val Terracotta10  = Color(0xFF3B1105)
    val Terracotta20  = Color(0xFF5B1C06)
    val Terracotta30  = Color(0xFF7E2E0F)
    val Terracotta40  = Color(0xFFA8431C)
    val Terracotta80  = Color(0xFFFFB59B)
    val Terracotta90  = Color(0xFFFFDBCF)

    val Sage10        = Color(0xFF0C2716)
    val Sage20        = Color(0xFF1D3728)
    val Sage30        = Color(0xFF33503E)
    val Sage40        = Color(0xFF4A7C59)
    val Sage80        = Color(0xFFB1CFBB)
    val Sage90        = Color(0xFFCDEBD6)

    val Gold10        = Color(0xFF261A00)
    val Gold20        = Color(0xFF3D2F05)
    val Gold30        = Color(0xFF5C4718)
    val Gold40        = Color(0xFF7A5C1F)
    val Gold80        = Color(0xFFDCC48D)
    val Gold90        = Color(0xFFF7E3AE)

    val Neutral6      = Color(0xFF130E0B)
    val Neutral10     = Color(0xFF191310)
    val Neutral12     = Color(0xFF211A16)
    val Neutral17     = Color(0xFF261E1A)
    val Neutral22     = Color(0xFF312824)
    val Neutral24     = Color(0xFF372F2A)
    val Neutral27     = Color(0xFF3C332E)
    val Neutral50     = Color(0xFF85736A)
    val Neutral60     = Color(0xFFA08D83)
    val Neutral90     = Color(0xFFEDE0DA)
    val Neutral94     = Color(0xFFF2EAE4)
    val Neutral96     = Color(0xFFF7F0EB)
    val Neutral98     = Color(0xFFFBF7F4)

    val NeutralVar30  = Color(0xFF52443C)
    val NeutralVar80  = Color(0xFFD7C4B9)
    val NeutralVar90  = Color(0xFFF0E4DC)
    val NeutralVar92  = Color(0xFFECE3DC)
    val NeutralVar88  = Color(0xFFE6DCD5)

    val Error10       = Color(0xFF410E0B)
    val Error20       = Color(0xFF601410)
    val Error30       = Color(0xFF8C1D18)
    val Error40       = Color(0xFFB3261E)
    val Error80       = Color(0xFFF2B8B5)
    val Error90       = Color(0xFFF9DEDC)
}

// ─── Accents the Material scheme has no slot for ──────────────────────────────

/** Door / window markers on the floor-plan toolbar and canvas. */
@Immutable
data class InteriorAccents(
    val door: Color,
    val onDoor: Color,
    val window: Color,
    val onWindow: Color,
    val canvasGrid: Color,
    val canvasRoomFill: Color,
)

val LightAccents = InteriorAccents(
    door           = Color(0xFFA8431C),
    onDoor         = Color.White,
    window         = Color(0xFF2A6099),
    onWindow       = Color.White,
    canvasGrid     = Color(0xFFD7C4B9),
    canvasRoomFill = Color(0x1AA8431C),
)

val DarkAccents = InteriorAccents(
    door           = Color(0xFFFFB59B),
    onDoor         = Color(0xFF5B1C06),
    window         = Color(0xFF9CC8F5),
    onWindow       = Color(0xFF06304F),
    canvasGrid     = Color(0xFF52443C),
    canvasRoomFill = Color(0x26FFB59B),
)

val LocalInteriorAccents = staticCompositionLocalOf { LightAccents }
