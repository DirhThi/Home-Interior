package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.catalog.FLOOR_PRESETS
import com.interiordesign3d.data.catalog.SurfacePreset
import com.interiordesign3d.data.catalog.WALL_PRESETS
import com.interiordesign3d.data.models.ColorPalette
import com.interiordesign3d.data.repository.ColorPaletteRepository
import com.interiordesign3d.ui.properties.AssetImage
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.properties.parseHexColor
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private val PAINT_COLORS = listOf(
    "#FFFFFF", "#F7F5EF", "#F0EBE3", "#EDE0D0", "#E8D5C4", "#F2C4A0",
    "#E8A87C", "#D4785A", "#A15A2C", "#6B3A16", "#E4EDE7", "#C6DDD0",
    "#86C7B0", "#2F5D50", "#173A30", "#E3F2FD", "#90CAF9", "#42A5F5",
    "#1565C0", "#0D47A1", "#F5F5F5", "#E0E0E0", "#9E9E9E", "#616161",
    "#212121", "#FFF9C4", "#FFE082", "#FFB300", "#FF7043", "#F4511E",
)

@Composable
fun SurfaceSheet(state: DesignerState, onDismiss: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(R.string.walls, R.string.floor, R.string.wall_colour, R.string.palettes)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Two view options that used to be full switch rows with subtitles — half the height as chips.
            CenterRow(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                Arrangement.spacedBy(8.dp),
            ) {
                OptionChip(
                    label = stringResource(R.string.hide_front_walls),
                    icon = Icons.Outlined.VisibilityOff,
                    checked = state.autoHideWalls,
                    onCheck = state::onAutoHideWalls,
                )
                OptionChip(
                    label = stringResource(R.string.shadows),
                    icon = Icons.Outlined.Contrast,
                    checked = state.shadowsOn,
                    onCheck = state::onShadows,
                )
            }

            ScrollableTabRow(
                selectedTabIndex = tab,
                containerColor = Color.Transparent,
                edgePadding = 20.dp,
                divider = {},
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(stringResource(label), style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }

            when (tab) {
                0 -> PresetRow(WALL_PRESETS, state.wallPresetIdx, state::onWallPreset)
                1 -> PresetRow(FLOOR_PRESETS, state.floorPresetIdx, state::onFloorPreset)
                2 -> PaintRow(state.wallColorOverride, state::onWallColor)
                else -> PaletteRow(onApply = state::onApplyPalette)
            }
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheck: (Boolean) -> Unit,
) {
    FilterChip(
        selected = checked,
        onClick = { onCheck(!checked) },
        modifier = Modifier.height(MinTouchTarget),
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
    )
}

@Composable
private fun PresetRow(presets: List<SurfacePreset>, selectedIdx: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        itemsIndexed(presets) { index, preset ->
            SurfaceTile(
                preset = preset,
                selected = index == selectedIdx,
                onClick = { onSelect(index) },
            )
        }
    }
}

/** Swatch over label — the texture is the thing being chosen, so show it big rather than as a chip icon. */
@Composable
private fun SurfaceTile(preset: SurfacePreset, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(76.dp).onClickNotRipple(onClick = onClick),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium,
                )
        ) {
            if (preset.isPaint) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(parseHexColor(preset.colorHex, Color.Gray))
                )
            } else {
                AssetImage(preset.preview, Modifier.size(64.dp))
            }
        }
        Text(
            preset.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun PaintRow(selected: String?, onSelect: (String?) -> Unit) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(MinTouchTarget * 2f),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        items(PAINT_COLORS, key = { it }) { hex ->
            CenterBox(Modifier.size(MinTouchTarget).onClickNotRipple { onSelect(hex) }) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(parseHexColor(hex, Color.Gray), CircleShape)
                        .border(
                            width = if (hex == selected) 3.dp else 1.dp,
                            color = if (hex == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                )
            }
        }
    }
}

@Composable
private fun PaletteRow(onApply: (ColorPalette) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        items(ColorPaletteRepository.palettes, key = { it.id }) { palette ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.width(150.dp).onClickNotRipple { onApply(palette) },
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(3.dp)) {
                        listOf(palette.primary, palette.secondary, palette.accent, palette.background)
                            .forEach { hex ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(parseHexColor(hex, Color.Gray))
                                )
                            }
                    }
                    Text(palette.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    Text(
                        palette.style.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
