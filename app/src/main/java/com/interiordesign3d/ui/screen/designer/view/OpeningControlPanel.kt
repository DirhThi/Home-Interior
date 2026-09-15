package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.DoorSliding
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Window
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
import com.interiordesign3d.data.models.OpeningType
import com.interiordesign3d.data.models.WallOpening
import com.interiordesign3d.ui.properties.ChoiceChip
import com.interiordesign3d.ui.properties.PanelIconButton
import com.interiordesign3d.ui.properties.ValueChip
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.NumberInputDialog
import com.interiordesign3d.ui.screen.designer.state.DesignerState

/**
 * Shown when a door or window on the plan is tapped. Hiding the leaf is what turns a door into a
 * cased opening — the pass-through between two rooms.
 */
@Composable
fun OpeningControlPanel(opening: WallOpening, state: DesignerState) {
    val isDoor = opening.type == OpeningType.DOOR
    var editingWidth by remember(opening.id) { mutableStateOf(false) }

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
                        if (isDoor) Icons.Outlined.DoorFront else Icons.Outlined.Window,
                        null,
                        Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(if (isDoor) R.string.door else R.string.window),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                PanelIconButton(
                    Icons.Outlined.Delete,
                    stringResource(R.string.remove_item),
                    onClick = state::onRemoveSelectedOpening,
                    danger = true,
                )
                PanelIconButton(
                    Icons.Outlined.Close,
                    stringResource(R.string.deselect),
                    onClick = { state.onSelectOpening(null) },
                )
            }

            CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                ChoiceChip(
                    label = stringResource(if (isDoor) R.string.show_leaf else R.string.show_frame),
                    selected = !opening.leafHidden,
                    icon = Icons.Outlined.Visibility,
                    onClick = { state.onSetLeafHidden(!opening.leafHidden) },
                )

                AnimatedVisibility(visible = isDoor && !opening.leafHidden) {
                    ChoiceChip(
                        label = stringResource(R.string.door_open),
                        selected = opening.leafOpen,
                        icon = Icons.Outlined.DoorSliding,
                        onClick = { state.onSetLeafOpen(!opening.leafOpen) },
                    )
                }

                ValueChip(stringResource(R.string.centimetres, opening.widthCm.toInt())) {
                    editingWidth = true
                }
            }
        }
    }

    if (editingWidth) {
        NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.opening_width)),
            suffix = "cm",
            initial = opening.widthCm,
            range = if (isDoor) 60f..200f else 40f..300f,
            step = 5f,
            onConfirm = { state.onResizeOpening(opening.id, it) },
            onDismiss = { editingWidth = false },
        )
    }
}
