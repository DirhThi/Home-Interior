package com.interiordesign3d.ui.screen.home.view

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseScreen
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.home.state.HomeState
import com.interiordesign3d.ui.screen.main.MainBottomInset
import com.interiordesign3d.ui.theme.LocalGlass

/** Two ways in: start something, or go looking for something. Nothing else competes with them. */
@Composable
fun HomeContent(state: HomeState) {
    BaseScreen(loading = state.loading) { modifier ->
        Column(
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                pluralStringResource(R.plurals.room_count, state.projectCount, state.projectCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )

            ActionCard(
                icon = Icons.Outlined.AddHome,
                title = R.string.card_new_title,
                body = R.string.card_new_body,
                accent = MaterialTheme.colorScheme.primary,
                onClick = state::onNewProject,
            )

            ActionCard(
                icon = Icons.Outlined.Chair,
                title = R.string.card_explore_title,
                body = R.string.card_explore_body,
                accent = MaterialTheme.colorScheme.tertiary,
                onClick = state::onExplore,
            )

            Spacer(Modifier.height(MainBottomInset))
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    @StringRes title: Int,
    @StringRes body: Int,
    accent: Color,
    onClick: () -> Unit,
) {
    val glass = LocalGlass.current

    GlassPane(
        modifier = Modifier.fillMaxWidth().onClickNotRipple(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        strong = true,
        elevation = 8.dp,
    ) {
        CenterRow(
            Modifier.fillMaxWidth().padding(18.dp),
            Arrangement.spacedBy(16.dp),
        ) {
            CenterBox(
                Modifier.size(56.dp).background(accent.copy(alpha = 0.16f), CircleShape)
            ) {
                Icon(icon, null, Modifier.size(26.dp), tint = accent)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                null,
                Modifier.size(20.dp),
                tint = glass.contentMuted,
            )
        }
    }
}
