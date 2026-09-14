package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.catalog.catalogItem
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.ui.properties.AssetImage
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.NumberInputDialog
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.properties.parseHexColor
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private val SWATCHES = listOf(
    null, "#D9C2A6", "#FFFFFF", "#9AA0A6", "#3B3B3B",
    "#C75D4F", "#E0A24B", "#5B7C99", "#6E8B5B", "#8E6FB0",
)

private const val TAB_SIZE = 0
private const val TAB_COLOUR = 1
private const val TAB_ROTATION = 2
private const val TAB_WALL = 3

/**
 * Same shape as the bottom sheets: one tab strip, one short row underneath. Stacking every
 * control vertically made the panel tall enough to hide the item being edited.
 */
@Composable
fun FurnitureControlPanel(item: PlacedFurniture, state: DesignerState) {
    var tab by remember { mutableIntStateOf(TAB_SIZE) }

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        shadowElevation = 16.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            PanelHeader(item = item, state = state)

            ScrollableTabRow(
                selectedTabIndex = tab,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                divider = {},
            ) {
                Tab(tab == TAB_SIZE, { tab = TAB_SIZE }, text = { TabLabel(R.string.furniture_scale) })
                Tab(tab == TAB_COLOUR, { tab = TAB_COLOUR }, text = { TabLabel(R.string.furniture_color) })
                if (item.isWallMounted) {
                    Tab(tab == TAB_ROTATION, { tab = TAB_ROTATION }, text = { TabLabel(R.string.height_from_floor) })
                } else {
                    Tab(tab == TAB_ROTATION, { tab = TAB_ROTATION }, text = { TabLabel(R.string.furniture_rotation) })
                }
                Tab(tab == TAB_WALL, { tab = TAB_WALL }, text = { TabLabel(R.string.wall_mount) })
            }

            Box(Modifier.fillMaxWidth().height(MinTouchTarget)) {
                when (tab) {
                    TAB_SIZE -> SliderRow(
                        label = stringResource(R.string.furniture_scale),
                        readout = stringResource(R.string.percent, (item.scale * 100).toInt()),
                        suffix = "%",
                        value = item.scale * 100f,
                        range = 50f..200f,
                        onChange = { state.onScale(it / 100f) },
                        sliderValue = item.scale,
                        sliderRange = 0.5f..2f,
                        onSlide = state::onScale,
                    )

                    TAB_COLOUR -> ColourRow(
                        selected = item.colorOverride,
                        onSelect = state::onColorChange,
                    )

                    TAB_ROTATION -> if (item.isWallMounted) {
                        SliderRow(
                            label = stringResource(R.string.height_from_floor),
                            readout = stringResource(R.string.centimetres, item.wallMountHeight.toInt()),
                            suffix = "cm",
                            value = item.wallMountHeight,
                            range = 40f..230f,
                            onChange = state::onChangeHeight,
                        )
                    } else {
                        SliderRow(
                            label = stringResource(R.string.furniture_rotation),
                            readout = stringResource(R.string.degrees, item.rotationY.toInt()),
                            suffix = "°",
                            value = item.rotationY,
                            range = 0f..360f,
                            onChange = state::onRotate,
                        )
                    }

                    else -> WallMountRow(item = item, state = state)
                }
            }
        }
    }
}

@Composable
private fun TabLabel(res: Int) {
    Text(stringResource(res), style = MaterialTheme.typography.labelLarge, maxLines = 1)
}

@Composable
private fun PanelHeader(item: PlacedFurniture, state: DesignerState) {
    CenterRow(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp),
        Arrangement.SpaceBetween,
    ) {
        CenterRow(Modifier.weight(1f), Arrangement.spacedBy(10.dp)) {
            catalogItem(item.furnitureId)?.preview?.let { AssetImage(it, Modifier.size(34.dp)) }
            Text(item.furnitureName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
        IconButton(
            onClick = state::onDeleteSelected,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Icon(Icons.Outlined.Delete, stringResource(R.string.remove_item))
        }
        IconButton(onClick = state::onDeselect) {
            Icon(Icons.Outlined.Close, stringResource(R.string.deselect))
        }
    }
}

/**
 * Slider plus a tappable readout. The slider and the typed value can run on different scales
 * (size slides 0.5..2 but is typed as 50..200 %), hence the separate slider parameters.
 */
@Composable
private fun SliderRow(
    label: String,
    readout: String,
    suffix: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    sliderValue: Float = value,
    sliderRange: ClosedFloatingPointRange<Float> = range,
    onSlide: (Float) -> Unit = onChange,
) {
    var editing by remember(label) { mutableStateOf(false) }

    CenterRow(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp)) {
        Slider(
            value = sliderValue,
            onValueChange = onSlide,
            valueRange = sliderRange,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(
            onClick = { editing = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(readout, style = MaterialTheme.typography.labelLarge)
        }
    }

    if (editing) {
        NumberInputDialog(
            title = stringResource(R.string.edit_value, label.lowercase()),
            suffix = suffix,
            initial = value,
            range = range,
            onConfirm = onChange,
            onDismiss = { editing = false },
        )
    }
}

@Composable
private fun ColourRow(selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(SWATCHES) { hex -> ColourSwatch(hex, hex == selected) { onSelect(hex) } }
    }
}

@Composable
private fun ColourSwatch(hex: String?, selected: Boolean, onClick: () -> Unit) {
    val fill = hex?.let { parseHexColor(it, Color.Gray) }
        ?: MaterialTheme.colorScheme.surfaceContainerHighest
    CenterBox(Modifier.size(MinTouchTarget).onClickNotRipple(onClick = onClick)) {
        CenterBox(
            Modifier
                .size(32.dp)
                .background(fill, CircleShape)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
        ) {
            if (hex == null) {
                Icon(
                    Icons.Outlined.FormatColorReset,
                    stringResource(R.string.default_colour),
                    Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WallMountRow(item: PlacedFurniture, state: DesignerState) {
    CenterRow(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(
                if (item.isWallMounted) R.string.wall_mount_on else R.string.wall_mount_off
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = item.isWallMounted, onCheckedChange = state::onToggleWallMount)
    }
}
