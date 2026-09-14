package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.properties.parseHexColor
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private val SWATCHES = listOf(
    null, "#D9C2A6", "#FFFFFF", "#9AA0A6", "#3B3B3B",
    "#C75D4F", "#E0A24B", "#5B7C99", "#6E8B5B", "#8E6FB0",
)

@Composable
fun FurnitureControlPanel(item: PlacedFurniture, state: DesignerState) {
    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PanelHeader(item = item, state = state)

            SliderRow(
                label = stringResource(R.string.furniture_scale),
                readout = stringResource(R.string.percent, (item.scale * 100).toInt()),
                value = item.scale,
                range = 0.5f..2f,
                onChange = state::onScale,
            )

            ColorSwatchRow(selected = item.colorOverride, onSelect = state::onColorChange)

            WallMountToggle(item = item, state = state)

            AnimatedVisibility(visible = item.isWallMounted) {
                SliderRow(
                    label = stringResource(R.string.height_from_floor),
                    readout = stringResource(R.string.centimetres, item.wallMountHeight.toInt()),
                    value = item.wallMountHeight,
                    range = 40f..230f,
                    onChange = state::onChangeHeight,
                )
            }

            AnimatedVisibility(visible = !item.isWallMounted) {
                SliderRow(
                    label = stringResource(R.string.furniture_rotation),
                    readout = stringResource(R.string.degrees, item.rotationY.toInt()),
                    value = item.rotationY,
                    range = 0f..360f,
                    onChange = state::onRotate,
                )
            }
        }
    }
}

@Composable
private fun PanelHeader(item: PlacedFurniture, state: DesignerState) {
    CenterRow(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        CenterRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            catalogItem(item.furnitureId)?.preview?.let { preview ->
                AssetImage(preview, Modifier.size(40.dp))
            }
            Text(item.furnitureName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
        CenterRow {
            IconButton(onClick = state::onDeleteSelected) {
                Icon(
                    Icons.Outlined.Delete,
                    stringResource(R.string.remove_item),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            IconButton(onClick = state::onDeselect) {
                Icon(Icons.Outlined.Close, stringResource(R.string.deselect))
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    readout: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column {
        CenterRow(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                readout,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun ColorSwatchRow(selected: String?, onSelect: (String?) -> Unit) {
    Column {
        Text(stringResource(R.string.furniture_color), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        CenterRow(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            Arrangement.spacedBy(4.dp),
        ) {
            SWATCHES.forEach { hex -> ColorSwatch(hex, hex == selected) { onSelect(hex) } }
        }
    }
}

@Composable
private fun ColorSwatch(hex: String?, selected: Boolean, onClick: () -> Unit) {
    val fill = hex?.let { parseHexColor(it, Color.Gray) } ?: MaterialTheme.colorScheme.surfaceContainerHighest
    CenterBox(Modifier.size(MinTouchTarget).onClickNotRipple(onClick = onClick)) {
        CenterBox(
            Modifier
                .size(34.dp)
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
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WallMountToggle(item: PlacedFurniture, state: DesignerState) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (item.isWallMounted) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        CenterRow(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            Arrangement.SpaceBetween,
        ) {
            CenterRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Outlined.Tv,
                    null,
                    Modifier.size(18.dp),
                    tint = if (item.isWallMounted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(stringResource(R.string.wall_mount), style = MaterialTheme.typography.labelLarge)
            }
            Switch(checked = item.isWallMounted, onCheckedChange = state::onToggleWallMount)
        }
    }
}
