package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Rotate90DegreesCw
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.FLOOR_SLAB_CM
import com.interiordesign3d.data.models.MIN_COMFORTABLE_TREAD_CM
import com.interiordesign3d.data.models.Stair
import com.interiordesign3d.data.models.StairShape
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.NumberInputDialog
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private const val EDIT_NONE = 0
private const val EDIT_WIDTH = 1
private const val EDIT_LENGTH = 2
private const val EDIT_LEG = 3
private const val EDIT_WELL = 4

@Composable
fun StairControlPanel(stair: Stair, state: DesignerState) {
    var editing by remember(stair.id) { mutableStateOf(EDIT_NONE) }

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 16.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CenterRow(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                CenterRow(Modifier.weight(1f), Arrangement.spacedBy(10.dp)) {
                    Icon(
                        Icons.Outlined.Stairs, null, Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.stairs), style = MaterialTheme.typography.titleMedium)
                }
                IconButton(
                    onClick = state::onRemoveSelectedStair,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(Icons.Outlined.Delete, stringResource(R.string.remove_item))
                }
                IconButton(onClick = { state.onSelectStair(null) }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.deselect))
                }
            }

            if (!state.floorPlan.stairFits(stair)) {
                Text(
                    stringResource(R.string.stair_no_opening),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            val tread = stair.treadCm(state.roomHeightCm + FLOOR_SLAB_CM)
            if (tread < MIN_COMFORTABLE_TREAD_CM) {
                Text(
                    stringResource(R.string.stair_tread_shallow, tread.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                StairShape.entries.forEach { shape ->
                    FilterChip(
                        selected = stair.shape == shape,
                        onClick = { state.onStairShape(shape) },
                        modifier = Modifier.height(MinTouchTarget),
                        label = {
                            Text(
                                stringResource(
                                    when (shape) {
                                        StairShape.STRAIGHT -> R.string.stair_straight
                                        StairShape.L_SHAPED -> R.string.stair_l
                                        StairShape.U_SHAPED -> R.string.stair_u
                                    }
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }

            CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = false,
                    onClick = { state.onStairRotate((stair.rotationDeg + 90f) % 360f) },
                    modifier = Modifier.height(MinTouchTarget),
                    leadingIcon = { Icon(Icons.Outlined.Rotate90DegreesCw, null, Modifier.size(18.dp)) },
                    label = { Text(stringResource(R.string.degrees, stair.rotationDeg.toInt())) },
                )
                TextButton(onClick = { editing = EDIT_WIDTH }) {
                    Text(stringResource(R.string.stair_width, stair.widthCm.toInt()))
                }
                TextButton(onClick = { editing = EDIT_LENGTH }) {
                    Text(stringResource(R.string.stair_length, stair.lengthCm.toInt()))
                }
                when (stair.shape) {
                    StairShape.L_SHAPED -> TextButton(onClick = { editing = EDIT_LEG }) {
                        Text(stringResource(R.string.stair_leg, stair.legCm.toInt()))
                    }
                    StairShape.U_SHAPED -> TextButton(onClick = { editing = EDIT_WELL }) {
                        Text(stringResource(R.string.stair_well, stair.wellCm.toInt()))
                    }
                    StairShape.STRAIGHT -> Unit
                }
            }
        }
    }

    when (editing) {
        EDIT_WIDTH -> NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.stair_width_label)),
            suffix = "cm",
            initial = stair.widthCm,
            range = 70f..200f,
            step = 5f,
            onConfirm = state::onStairWidth,
            onDismiss = { editing = EDIT_NONE },
        )
        EDIT_LENGTH -> NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.stair_length_label)),
            suffix = "cm",
            initial = stair.lengthCm,
            range = 180f..500f,
            step = 10f,
            onConfirm = state::onStairLength,
            onDismiss = { editing = EDIT_NONE },
        )
        EDIT_LEG -> NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.stair_leg_label)),
            suffix = "cm",
            initial = stair.legCm,
            range = 100f..400f,
            step = 10f,
            onConfirm = state::onStairLeg,
            onDismiss = { editing = EDIT_NONE },
        )
        EDIT_WELL -> NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.stair_well_label)),
            suffix = "cm",
            initial = stair.wellCm,
            range = 0f..60f,
            step = 5f,
            onConfirm = state::onStairWell,
            onDismiss = { editing = EDIT_NONE },
        )
    }
}
