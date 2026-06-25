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
    onChangeDims: (Float, Float, Float) -> Unit
) {
    val catIcon = try { FurnitureCategory.valueOf(item.furnitureId).icon } catch (e: Exception) { "🛋️" }
    val baseW = item.customWidthCm.takeIf  { it > 0f } ?: 60f
    val baseD = item.customDepthCm.takeIf  { it > 0f } ?: 60f
    val baseH = item.customHeightCm.takeIf { it > 0f } ?: 40f

    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(catIcon, style = MaterialTheme.typography.titleLarge)
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

            HorizontalDivider()

            // ── Dimension sliders ──────────────────────────────────────────────
            DimSlider("Width",  baseW, 20f..1000f, displayValue = baseW * item.scale) { onChangeDims(it, baseD, baseH) }
            if (item.isWallMounted)
                DimSlider("Thickness", baseD, 2f..100f, displayValue = baseD * item.scale) { onChangeDims(baseW, it, baseH) }
            else
                DimSlider("Depth", baseD, 20f..1000f, displayValue = baseD * item.scale) { onChangeDims(baseW, it, baseH) }
            DimSlider("Height", baseH, 10f..600f, displayValue = baseH * item.scale) { onChangeDims(baseW, baseD, it) }

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
