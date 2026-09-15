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
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.WallStyle
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.MinTouchTarget
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
                CenterRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Outlined.ViewWeek, null, Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.wall), style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = { state.onSelectWall(null) }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.deselect))
                }
            }

            CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                WallStyle.entries.forEach { s ->
                    FilterChip(
                        selected = style == s,
                        onClick = { state.onWallStyle(s) },
                        modifier = Modifier.height(MinTouchTarget),
                        label = {
                            Text(
                                stringResource(
                                    when (s) {
                                        WallStyle.FULL -> R.string.wall_full
                                        WallStyle.HALF -> R.string.wall_half
                                        WallStyle.OPEN -> R.string.wall_open
                                    }
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }

            // Only an open wall stands on columns, so the choice appears only then.
            if (style == WallStyle.OPEN) {
                CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    COLUMNS.forEach { (key, label) ->
                        FilterChip(
                            selected = columnKey == key,
                            onClick = { state.onWallColumn(key) },
                            modifier = Modifier.height(MinTouchTarget),
                            label = {
                                Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
                            },
                        )
                    }
                }
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
