package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.RoofShape
import com.interiordesign3d.ui.properties.ChoiceChip
import com.interiordesign3d.ui.properties.Segment
import com.interiordesign3d.ui.properties.SegmentedPills
import com.interiordesign3d.ui.properties.ValueChip
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.NumberInputDialog
import com.interiordesign3d.ui.screen.designer.state.DesignerState

private const val EDIT_NONE = 0
private const val EDIT_PITCH = 1
private const val EDIT_EAVES = 2
private const val EDIT_TAPER = 3

/**
 * Roof shape and its numbers, parked at the bottom of the exterior view. They used to sit three
 * levels down — sheet, tab, button — while the thing they change filled the screen behind, so nobody
 * found them. Materials stay in the surfaces sheet with the other swatch rows.
 */
@Composable
fun RoofControlPanel(state: DesignerState) {
    var editing by remember { mutableIntStateOf(EDIT_NONE) }
    val ext = state.exteriorSurface
    val flat = ext.roofShape == RoofShape.FLAT

    GlassPane(
        shape = MaterialTheme.shapes.extraLarge,
        strong = true,
        elevation = 18.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val shapes = RoofShape.entries
            SegmentedPills(
                segments = shapes.map {
                    Segment(
                        stringResource(
                            when (it) {
                                RoofShape.FLAT -> R.string.roof_flat
                                RoofShape.HIP -> R.string.roof_hip
                                RoofShape.THAI -> R.string.roof_thai
                            }
                        )
                    )
                },
                selectedIndex = shapes.indexOf(ext.roofShape),
                onSelect = { state.onRoofShape(shapes[it]) },
                modifier = Modifier.fillMaxWidth(),
            )

            if (!flat) {
                CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    ValueChip(stringResource(R.string.roof_pitch, ext.pitch.toInt())) { editing = EDIT_PITCH }
                    ValueChip(stringResource(R.string.roof_eaves, ext.eaves.toInt())) { editing = EDIT_EAVES }
                    ValueChip(stringResource(R.string.roof_taper, (ext.hipFactor * 100f).toInt())) { editing = EDIT_TAPER }
                }
            }

            // Only worth offering when there is a storey under the top one to reach out over.
            if (state.topLevel > 0) {
                ChoiceChip(
                    label = stringResource(R.string.roof_cover_terrace),
                    selected = ext.coverTerrace,
                    onClick = { state.onRoofCoverTerrace(!ext.coverTerrace) },
                )
            }

            // Say what this pick does to THIS plan, so the 3D behind is never a surprise.
            val note = when {
                flat -> R.string.roof_note_flat
                !state.roofCanPitch -> R.string.roof_note_slanted
                ext.roofShape == RoofShape.HIP && state.roofMassCount > 1 -> R.string.roof_note_covers_notch
                ext.roofShape == RoofShape.THAI && state.roofMassCount == 1 -> R.string.roof_note_single_mass
                else -> null
            }
            when {
                note != null -> Text(
                    stringResource(note),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.roofCanPitch) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error,
                )

                state.roofMassCount > 1 -> Text(
                    pluralStringResource(
                        R.plurals.roof_masses, state.roofMassCount, state.roofMassCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    when (editing) {
        EDIT_PITCH -> NumberInputDialog(
            title = stringResource(R.string.roof_pitch_label), suffix = "°",
            initial = ext.pitch, range = 5f..55f, step = 1f,
            onConfirm = state::onRoofPitch, onDismiss = { editing = EDIT_NONE },
        )
        EDIT_EAVES -> NumberInputDialog(
            title = stringResource(R.string.roof_eaves_label), suffix = "cm",
            initial = ext.eaves, range = 0f..150f, step = 5f,
            onConfirm = state::onRoofEaves, onDismiss = { editing = EDIT_NONE },
        )
        EDIT_TAPER -> NumberInputDialog(
            title = stringResource(R.string.roof_taper_label), suffix = "%",
            initial = ext.hipFactor * 100f, range = 0f..100f, step = 10f,
            onConfirm = state::onRoofHipFactor, onDismiss = { editing = EDIT_NONE },
        )
    }
}
