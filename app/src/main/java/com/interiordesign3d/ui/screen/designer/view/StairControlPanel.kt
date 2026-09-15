package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Rotate90DegreesCw
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.interiordesign3d.ui.properties.ChoiceChip
import com.interiordesign3d.ui.properties.PanelIconButton
import com.interiordesign3d.ui.properties.Segment
import com.interiordesign3d.ui.properties.SegmentedPills
import com.interiordesign3d.ui.properties.ValueChip
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.CenterRow
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

    GlassPane(
        shape = MaterialTheme.shapes.extraLarge,
        strong = true,
        elevation = 18.dp,
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
                PanelIconButton(
                    Icons.Outlined.Delete,
                    stringResource(R.string.remove_item),
                    onClick = state::onRemoveSelectedStair,
                    danger = true,
                )
                PanelIconButton(
                    Icons.Outlined.Close,
                    stringResource(R.string.deselect),
                    onClick = { state.onSelectStair(null) },
                )
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

            val shapes = StairShape.entries
            SegmentedPills(
                segments = shapes.map {
                    Segment(
                        stringResource(
                            when (it) {
                                StairShape.STRAIGHT -> R.string.stair_straight
                                StairShape.L_SHAPED -> R.string.stair_l
                                StairShape.U_SHAPED -> R.string.stair_u
                            }
                        )
                    )
                },
                selectedIndex = shapes.indexOf(stair.shape),
                onSelect = { state.onStairShape(shapes[it]) },
                modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
            )

            CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                ChoiceChip(
                    label = stringResource(R.string.degrees, stair.rotationDeg.toInt()),
                    selected = false,
                    icon = Icons.Outlined.Rotate90DegreesCw,
                    onClick = { state.onStairRotate((stair.rotationDeg + 90f) % 360f) },
                )
                ValueChip(stringResource(R.string.stair_width, stair.widthCm.toInt())) { editing = EDIT_WIDTH }
                ValueChip(stringResource(R.string.stair_length, stair.lengthCm.toInt())) { editing = EDIT_LENGTH }
                when (stair.shape) {
                    StairShape.L_SHAPED ->
                        ValueChip(stringResource(R.string.stair_leg, stair.legCm.toInt())) { editing = EDIT_LEG }
                    StairShape.U_SHAPED ->
                        ValueChip(stringResource(R.string.stair_well, stair.wellCm.toInt())) { editing = EDIT_WELL }
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
