package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import com.interiordesign3d.ui.theme.LocalGlass

/** Ground floor is 1. Hidden until there is a reason to show it — a single-storey plan needs no switch. */
@Composable
fun LevelSwitcher(state: DesignerState, modifier: Modifier = Modifier) {
    val glass = LocalGlass.current

    GlassPane(modifier, shape = CircleShape, elevation = 8.dp) {
        CenterRow(Modifier.padding(3.dp), Arrangement.spacedBy(2.dp)) {
            repeat(state.levelCount) { level ->
                val selected = level == state.activeLevel
                CenterBox(
                    Modifier
                        .size(MinTouchTarget)
                        .clip(CircleShape)
                        .then(if (selected) Modifier.background(glass.accent) else Modifier)
                        .onClickNotRipple { state.onSelectLevel(level) }
                ) {
                    Text(
                        "${level + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) glass.onAccent else glass.contentMuted,
                    )
                }
            }
            CenterBox(
                Modifier.size(MinTouchTarget).onClickNotRipple { state.onAddLevel() }
            ) {
                Icon(
                    Icons.Outlined.Add,
                    stringResource(R.string.add_level),
                    Modifier.size(20.dp),
                    tint = glass.content,
                )
            }
        }
    }
}
