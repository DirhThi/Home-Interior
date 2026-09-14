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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.interiordesign3d.data.repository.ColorPaletteRepository
import com.interiordesign3d.ui.properties.AssetImage
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.properties.parseHexColor
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private val PAINT_COLORS = listOf(
    "#FFFFFF", "#F5F0EB", "#F0EBE3", "#EDE0D0", "#E8D5C4", "#F2C4A0",
    "#E8A87C", "#D4785A", "#B5451B", "#8A3210", "#E8F5E9", "#C8E6C9",
    "#81C784", "#4A7C59", "#2C5F3E", "#E3F2FD", "#90CAF9", "#42A5F5",
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
            Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ToggleRow(
                    title = stringResource(R.string.hide_front_walls),
                    subtitle = stringResource(R.string.hide_front_walls_desc),
                    checked = state.autoHideWalls,
                    onChange = state::onAutoHideWalls,
                )
                ToggleRow(
                    title = stringResource(R.string.shadows),
                    subtitle = stringResource(
                        if (state.shadowsOn) R.string.shadows_on else R.string.shadows_off
                    ),
                    checked = state.shadowsOn,
                    onChange = state::onShadows,
                )
            }

            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
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
                2 -> PaintGrid(state.wallColorOverride, state::onWallColor)
                else -> PaletteList(onApply = state::onApplyPalette)
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    CenterRow(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Column(Modifier.padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun PresetRow(presets: List<SurfacePreset>, selectedIdx: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        itemsIndexed(presets) { index, preset ->
            FilterChip(
                selected = index == selectedIdx,
                onClick = { onSelect(index) },
                modifier = Modifier.height(MinTouchTarget),
                leadingIcon = { SurfaceSwatch(preset) },
                label = { Text(preset.label, style = MaterialTheme.typography.labelLarge) },
            )
        }
    }
}

@Composable
private fun SurfaceSwatch(preset: SurfacePreset) {
    val modifier = Modifier.size(22.dp).clip(CircleShape)
    if (preset.isPaint) {
        Box(modifier.background(parseHexColor(preset.colorHex, Color.Gray)))
    } else {
        AssetImage(preset.preview, modifier)
    }
}

@Composable
private fun PaintGrid(selected: String?, onSelect: (String?) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = MinTouchTarget),
        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        items(PAINT_COLORS, key = { it }) { hex ->
            CenterBox(
                Modifier.size(MinTouchTarget).onClickNotRipple { onSelect(hex) }
            ) {
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
private fun PaletteList(onApply: (com.interiordesign3d.data.models.ColorPalette) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(ColorPaletteRepository.palettes, key = { it.id }) { palette ->
            Column {
                CenterRow(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text(palette.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            palette.style.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { onApply(palette) }) {
                        Text(stringResource(R.string.apply))
                    }
                }
                Spacer(Modifier.height(4.dp))
                CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    listOf(palette.primary, palette.secondary, palette.accent, palette.background)
                        .forEach { hex ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(parseHexColor(hex, Color.Gray))
                            )
                        }
                }
            }
        }
    }
}
