package com.interiordesign3d.ui.screen.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.data.repository.ThemeMode
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.Segment
import com.interiordesign3d.ui.properties.SegmentedPills
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.onClickNotRipple

private data class ThemeOption(val mode: ThemeMode, val icon: ImageVector, val label: Int)

private val THEME_OPTIONS = listOf(
    ThemeOption(ThemeMode.SYSTEM, Icons.Outlined.BrightnessAuto, R.string.theme_system),
    ThemeOption(ThemeMode.LIGHT, Icons.Outlined.LightMode, R.string.theme_light),
    ThemeOption(ThemeMode.DARK, Icons.Outlined.DarkMode, R.string.theme_dark),
)

@Composable
fun SettingsSheet(onShowCredits: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleLarge)

            Text(
                stringResource(R.string.appearance),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SegmentedPills(
                segments = THEME_OPTIONS.map { Segment(stringResource(it.label), it.icon) },
                selectedIndex = THEME_OPTIONS.indexOfFirst { it.mode == AppPrefs.themeMode }
                    .coerceAtLeast(0),
                onSelect = { AppPrefs.updateTheme(THEME_OPTIONS[it].mode) },
                modifier = Modifier.fillMaxWidth(),
            )

            CenterRow(
                Modifier
                    .fillMaxWidth()
                    .height(MinTouchTarget)
                    .onClickNotRipple(onClick = onShowCredits),
                Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Info,
                    null,
                    Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.model_credits),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
