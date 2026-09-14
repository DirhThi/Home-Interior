package com.interiordesign3d.ui.screen.home.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.ui.theme.LocalInteriorAccents

/** Draws the plan's rooms scaled to fit the box — no bitmap needed for a preview. */
@Composable
fun PlanThumbnail(plan: FloorPlan, modifier: Modifier = Modifier) {
    val accents = LocalInteriorAccents.current
    val fill = accents.canvasRoomFill
    val stroke = accents.canvasWall

    Canvas(modifier) {
        val nodes = plan.nodes
        if (nodes.isEmpty() || plan.rooms.isEmpty()) return@Canvas

        val minX = nodes.minOf { it.x }
        val maxX = nodes.maxOf { it.x }
        val minY = nodes.minOf { it.y }
        val maxY = nodes.maxOf { it.y }
        val planW = (maxX - minX).coerceAtLeast(1f)
        val planH = (maxY - minY).coerceAtLeast(1f)

        val pad = 10f
        val scale = minOf((size.width - pad * 2) / planW, (size.height - pad * 2) / planH)
        val offX = (size.width - planW * scale) / 2f - minX * scale
        val offY = (size.height - planH * scale) / 2f - minY * scale

        plan.rooms.forEach { room ->
            val path = Path()
            room.forEachIndexed { i, idx ->
                val p = nodes[idx]
                val x = offX + p.x * scale
                val y = offY + p.y * scale
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, fill)
            drawPath(path, stroke.copy(alpha = 0.75f), style = Stroke(1.6f))
        }
    }
}
