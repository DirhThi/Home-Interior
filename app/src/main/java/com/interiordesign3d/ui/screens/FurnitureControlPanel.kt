package com.interiordesign3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*

// ─── Furniture Control Panel ──────────────────────────────────────────────────

@Composable
internal fun FurnitureControlPanel(
    item: PlacedFurniture,
    onRotate: (Float) -> Unit,
    onScale: (Float) -> Unit,
    onDelete: () -> Unit,
    onDeselect: () -> Unit,
    onToggleWallMount: (Boolean) -> Unit,
    onChangeHeight: (Float) -> Unit,
    onColorChange: (String?) -> Unit = {},
) {
    val preview = catalogItem(item.furnitureId)?.preview

    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (preview != null) AssetImage(preview, Modifier.size(36.dp))
                    else Text("🛋️", style = MaterialTheme.typography.titleLarge)
                    Text(item.furnitureName, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                }
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onDeselect) { Icon(Icons.Filled.Close, "Deselect") }
                }
            }

            // ── Scale ──────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Scale", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("${(item.scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = item.scale, onValueChange = onScale,
                    valueRange = 0.5f..2.0f, modifier = Modifier.fillMaxWidth())
            }

            // ── Color ──────────────────────────────────────────────────────────
            Text("Màu", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val swatches = listOf(
                    null, "#D9C2A6", "#FFFFFF", "#9AA0A6", "#3B3B3B",
                    "#C75D4F", "#E0A24B", "#5B7C99", "#6E8B5B", "#8E6FB0"
                )
                swatches.forEach { hex ->
                    val sel = item.colorOverride == hex
                    Box(
                        Modifier.size(30.dp)
                            .background(hex?.let { parseColor(it, Color.Gray) } ?: MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .border(if (sel) 3.dp else 1.dp,
                                if (sel) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.15f),
                                CircleShape)
                            .clickable { onColorChange(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (hex == null) Icon(Icons.Filled.FormatColorReset, "Mặc định",
                            Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Wall mount toggle ──────────────────────────────────────────────
            Surface(shape = RoundedCornerShape(10.dp),
                color = if (item.isWallMounted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Tv, null, Modifier.size(18.dp),
                            tint = if (item.isWallMounted) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Wall Mount", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Switch(checked = item.isWallMounted, onCheckedChange = onToggleWallMount)
                }
            }

            // ── Height from floor (wall-mount only) ────────────────────────────
            AnimatedVisibility(visible = item.isWallMounted) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Height from floor", style = MaterialTheme.typography.labelMedium)
                        Text("${item.wallMountHeight.toInt()} cm",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = item.wallMountHeight, onValueChange = onChangeHeight,
                        valueRange = 40f..230f, modifier = Modifier.fillMaxWidth())
                }
            }

            // ── Rotation (floor items only) ────────────────────────────────────
            AnimatedVisibility(visible = !item.isWallMounted) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Rotation", style = MaterialTheme.typography.labelMedium)
                        Text("${item.rotationY.toInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = item.rotationY, onValueChange = onRotate,
                        valueRange = 0f..360f, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
