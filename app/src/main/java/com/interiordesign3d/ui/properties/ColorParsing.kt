package com.interiordesign3d.ui.properties

import androidx.compose.ui.graphics.Color

/** Parses "#RRGGBB"; returns [fallback] for anything Android's parser rejects. */
fun parseHexColor(hex: String?, fallback: Color): Color =
    hex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: fallback
