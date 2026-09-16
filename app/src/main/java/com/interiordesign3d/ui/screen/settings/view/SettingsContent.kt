package com.interiordesign3d.ui.screen.settings.view

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseScreen
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.data.repository.ThemeMode
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.Segment
import com.interiordesign3d.ui.properties.SegmentedPills
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.project.view.ModelCreditsDialog
import com.interiordesign3d.ui.screen.settings.state.SettingsState

private data class ThemeOption(val mode: ThemeMode, val icon: ImageVector, @param:StringRes val label: Int)

private val THEME_OPTIONS = listOf(
    ThemeOption(ThemeMode.SYSTEM, Icons.Outlined.BrightnessAuto, R.string.theme_system),
    ThemeOption(ThemeMode.LIGHT, Icons.Outlined.LightMode, R.string.theme_light),
    ThemeOption(ThemeMode.DARK, Icons.Outlined.DarkMode, R.string.theme_dark),
)

/** A tab, not a sheet — settings is where options accumulate, so it gets room to grow. */
@Composable
fun SettingsContent(state: SettingsState) {
    BaseScreen(loading = state.loading) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            item {
                SettingsSection(R.string.appearance) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SegmentedPills(
                        segments = THEME_OPTIONS.map { Segment(stringResource(it.label), it.icon) },
                        selectedIndex = THEME_OPTIONS.indexOfFirst { it.mode == AppPrefs.themeMode }
                            .coerceAtLeast(0),
                        onSelect = { AppPrefs.updateTheme(THEME_OPTIONS[it].mode) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SettingsRow(Icons.Outlined.Language, R.string.language, state::onLanguage)
                    }
                }
            }

            item {
                SettingsSection(R.string.about) {
                    SettingsRow(Icons.Outlined.Info, R.string.model_credits, state::onShowCredits)
                }
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }

    if (state.showCredits) {
        ModelCreditsDialog(onDismiss = state::onDismissCredits)
    }
}

@Composable
private fun SettingsSection(@StringRes header: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GlassPane(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            strong = true,
            elevation = 6.dp,
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, @StringRes label: Int, onClick: () -> Unit) {
    CenterRow(
        Modifier.fillMaxWidth().height(MinTouchTarget).onClickNotRipple(onClick = onClick),
        Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
    }
}
