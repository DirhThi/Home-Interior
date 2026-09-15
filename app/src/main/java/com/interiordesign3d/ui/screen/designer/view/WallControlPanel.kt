package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.WallStyle
import com.interiordesign3d.ui.properties.PanelIconButton
import com.interiordesign3d.ui.properties.Segment
import com.interiordesign3d.ui.properties.SegmentedPills
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.screen.designer.state.DesignerState

/** The catalogue's own columns, so a structural one is the same object you can drop in by hand. */
private val COLUMNS = listOf(
    "q_column_round" to R.string.column_round,
    "q_column_squarebig" to R.string.column_square,
    "q_column_squaresmall" to R.string.column_square_short,
)

@Composable
fun WallControlPanel(state: DesignerState) {
    val style = state.selectedWallStyle?.style ?: WallStyle.FULL
    val columnKey = state.selectedWallStyle?.columnKey ?: COLUMNS.first().first

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
                        Icons.Outlined.ViewWeek, null, Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.wall), style = MaterialTheme.typography.titleMedium)
                }
                PanelIconButton(
                    Icons.Outlined.Close,
                    stringResource(R.string.deselect),
                    onClick = { state.onSelectWall(null) },
                )
            }

            val styles = WallStyle.entries
            SegmentedPills(
                segments = styles.map {
                    Segment(
                        stringResource(
                            when (it) {
                                WallStyle.FULL -> R.string.wall_full
                                WallStyle.HALF -> R.string.wall_half
                                WallStyle.OPEN -> R.string.wall_open
                            }
                        )
                    )
                },
                selectedIndex = styles.indexOf(style),
                onSelect = { state.onWallStyle(styles[it]) },
                modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
            )

            // Only an open wall stands on columns, so the choice appears only then.
            if (style == WallStyle.OPEN) {
                SegmentedPills(
                    segments = COLUMNS.map { Segment(stringResource(it.second)) },
                    selectedIndex = COLUMNS.indexOfFirst { it.first == columnKey }.coerceAtLeast(0),
                    onSelect = { state.onWallColumn(COLUMNS[it].first) },
                    modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                )
            }

            Text(
                stringResource(
                    when (style) {
                        WallStyle.FULL -> R.string.wall_note_full
                        WallStyle.HALF -> R.string.wall_note_half
                        WallStyle.OPEN -> R.string.wall_note_open
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
