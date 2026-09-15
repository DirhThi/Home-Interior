package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balcony
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.Balcony
import com.interiordesign3d.ui.properties.PanelIconButton
import com.interiordesign3d.ui.properties.ValueChip
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.NumberInputDialog
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private const val EDIT_NONE = 0
private const val EDIT_WIDTH = 1
private const val EDIT_DEPTH = 2

@Composable
fun BalconyControlPanel(balcony: Balcony, state: DesignerState) {
    var editing by remember(balcony.id) { mutableIntStateOf(EDIT_NONE) }

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
                CenterRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Outlined.Balcony,
                        null,
                        Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(stringResource(R.string.balcony), style = MaterialTheme.typography.titleMedium)
                }
                PanelIconButton(
                    Icons.Outlined.Delete,
                    stringResource(R.string.remove_item),
                    onClick = state::onRemoveSelectedBalcony,
                    danger = true,
                )
                PanelIconButton(
                    Icons.Outlined.Close,
                    stringResource(R.string.deselect),
                    onClick = { state.onSelectBalcony(null) },
                )
            }

            CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                ValueChip(stringResource(R.string.balcony_width, balcony.widthCm.toInt())) { editing = EDIT_WIDTH }
                ValueChip(stringResource(R.string.balcony_depth, balcony.depthCm.toInt())) { editing = EDIT_DEPTH }
            }
        }
    }

    when (editing) {
        EDIT_WIDTH -> NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.balcony_width_label)),
            suffix = "cm", initial = balcony.widthCm, range = 90f..2000f, step = 10f,
            onConfirm = state::onBalconyWidth, onDismiss = { editing = EDIT_NONE },
        )
        EDIT_DEPTH -> NumberInputDialog(
            title = stringResource(R.string.edit_value, stringResource(R.string.balcony_depth_label)),
            suffix = "cm", initial = balcony.depthCm, range = 60f..300f, step = 10f,
            onConfirm = state::onBalconyDepth, onDismiss = { editing = EDIT_NONE },
        )
    }
}
