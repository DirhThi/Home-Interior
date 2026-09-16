package com.interiordesign3d.ui.screen.language.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.repository.LanguageManager
import com.interiordesign3d.ui.properties.AdSlot
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassIconButton
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.language.state.LanguageState

/**
 * Shared by all three ways into this list: the first-open base step, its Alt step, and the
 * Settings entry. [showBack] and [adPlacement] are the only things that tell them apart.
 */
@Composable
fun LanguageContent(state: LanguageState, showBack: Boolean = false, adPlacement: String? = null) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                CenterRow(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    Arrangement.spacedBy(12.dp),
                ) {
                    if (showBack) {
                        GlassIconButton(
                            icon = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            onClick = state::onBack,
                        )
                    }
                    Text(
                        stringResource(R.string.language),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            items(LanguageManager.codes, key = { it }) { code ->
                LanguageRow(
                    code = code,
                    selected = code == state.picked,
                    onClick = { state.onPick(code) },
                )
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassPillButton(
                icon = Icons.Outlined.Check,
                label = stringResource(R.string.apply),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = state::onConfirm,
            )
            adPlacement?.let {
                AdSlot(nameSpace = it, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))
            }
        }
    }
}

/** Endonym first: this list is read by someone who does not yet read the current language. */
@Composable
private fun LanguageRow(code: String, selected: Boolean, onClick: () -> Unit) {
    GlassPane(
        modifier = Modifier.fillMaxWidth().onClickNotRipple(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        strong = true,
        elevation = 4.dp,
    ) {
        CenterRow(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    LanguageManager.nativeName(code),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    LanguageManager.englishName(code),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
