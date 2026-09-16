package com.interiordesign3d.ui.screen.select.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Desk
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.repository.SpaceKind
import com.interiordesign3d.ui.properties.AdSlot
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.select.state.SelectState

/** Label and icon for each kind — the enum itself stays in the data layer, free of UI types. */
private val SpaceKind.label: Int
    get() = when (this) {
        SpaceKind.WholeHouse -> R.string.kind_house
        SpaceKind.Apartment -> R.string.kind_apartment
        SpaceKind.SingleRoom -> R.string.kind_room
        SpaceKind.Workspace -> R.string.kind_workspace
    }

private val SpaceKind.icon: ImageVector
    get() = when (this) {
        SpaceKind.WholeHouse -> Icons.Outlined.Home
        SpaceKind.Apartment -> Icons.Outlined.Apartment
        SpaceKind.SingleRoom -> Icons.Outlined.Weekend
        SpaceKind.Workspace -> Icons.Outlined.Desk
    }

/**
 * Asks once what the person is designing. Nothing in the app branches on it yet — it is here to
 * learn what people actually open this for, and it is skippable for exactly that reason.
 */
@Composable
fun SelectContent(state: SelectState) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 64.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.select_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                stringResource(R.string.select_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            SpaceKind.entries.forEach { kind ->
                KindRow(
                    kind = kind,
                    selected = state.picked == kind,
                    onClick = { state.onPick(kind) },
                )
            }
        }

        Text(
            stringResource(R.string.skip),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .onClickNotRipple(onClick = state::onSkip)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        )

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassPillButton(
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                label = stringResource(R.string.ob_start),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                onClick = state::onContinue,
            )
            AdSlot(nameSpace = "select_bottom", modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
        }
    }
}

@Composable
private fun KindRow(kind: SpaceKind, selected: Boolean, onClick: () -> Unit) {
    GlassPane(
        modifier = Modifier.fillMaxWidth().onClickNotRipple(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        strong = true,
        elevation = 4.dp,
    ) {
        CenterRow(
            Modifier.fillMaxWidth().padding(16.dp),
            Arrangement.spacedBy(14.dp),
        ) {
            CenterBox(
                Modifier.size(44.dp).background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape
                )
            ) {
                Icon(
                    kind.icon,
                    null,
                    Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                stringResource(kind.label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                CenterBox(
                    Modifier.size(26.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}
