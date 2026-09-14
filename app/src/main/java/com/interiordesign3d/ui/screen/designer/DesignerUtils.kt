package com.interiordesign3d.ui.screen.designer

import androidx.compose.ui.graphics.Color
import com.interiordesign3d.data.models.WallPoint
import com.interiordesign3d.ui.properties.parseHexColor
import kotlin.math.*

// ─── Grid snap ───────────────────────────────────────────────────────────────

fun Float.snapTo(grid: Float) = if (grid > 0f) (this / grid).roundToInt() * grid else this

// ─── Color helpers ────────────────────────────────────────────────────────────

fun parseColor(hex: String, fallback: Color): Color = parseHexColor(hex, fallback)

operator fun Color.times(f: Float) = Color(red * f, green * f, blue * f, alpha)

// ─── Geometry ─────────────────────────────────────────────────────────────────

fun polygonArea(pts: List<WallPoint>): Float {
    var area = 0f
    val n = pts.size
    for (i in 0 until n) {
        val j = (i + 1) % n
        area += pts[i].x * pts[j].y - pts[j].x * pts[i].y
    }
    return abs(area) / 2f
}

// ─── Wall projection ──────────────────────────────────────────────────────────

/**
 * Find nearest point on any wall segment to (px, pz).
 * @param polygons  list of room polygons, each a list of WallPoint (x,y = room cm coords)
 */
fun findNearestWall(px: Float, pz: Float, polygons: List<List<WallPoint>>): WallInfo? {
    var bestDist = Float.MAX_VALUE
    var best: WallInfo? = null
    for (poly in polygons) {
        if (poly.size < 2) continue
        val cx = poly.map { it.x }.average().toFloat()
        val cz = poly.map { it.y }.average().toFloat()
        for (i in poly.indices) {
            val a = poly[i]; val b = poly[(i + 1) % poly.size]
            val edx = b.x - a.x; val edz = b.y - a.y
            val len = sqrt(edx * edx + edz * edz).coerceAtLeast(0.001f)
            val tx = edx / len; val tz = edz / len
            val t = ((px - a.x) * tx + (pz - a.y) * tz).coerceIn(0f, len)
            val sx = a.x + t * tx; val sz = a.y + t * tz
            val dist = sqrt((px - sx).pow(2) + (pz - sz).pow(2))
            if (dist < bestDist) {
                bestDist = dist
                // Pick normal direction that faces toward room centroid
                val nx0 = -tz; val nz0 = tx
                val midX = (a.x + b.x) / 2f; val midZ = (a.y + b.y) / 2f
                val (nx, nz) = if (nx0 * (cx - midX) + nz0 * (cz - midZ) > 0f) nx0 to nz0 else tz to -tx
                best = WallInfo(sx, sz, nx, nz, tx, tz)
            }
        }
    }
    return best
}
