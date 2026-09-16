package com.interiordesign3d.ui.screen.project.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseScreen
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassIconButton
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.screen.main.MainBottomInset
import com.interiordesign3d.ui.screen.project.state.ProjectState

/**
 * The header scrolls with the list rather than sitting in a `TopAppBar`. A room list is browsed, not
 * navigated from, so a bar pinned across the top spends permanent height on a title nobody re-reads.
 */
@Composable
fun ProjectContent(
    state: ProjectState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    BaseScreen(
        loading = state.loading,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            if (state.rooms.isNotEmpty()) {
                GlassPillButton(
                    icon = Icons.Outlined.Add,
                    label = stringResource(R.string.new_room),
                    // Lifted clear of the floating tab bar, which the Scaffold knows nothing about.
                    modifier = Modifier.padding(bottom = MainBottomInset),
                    onClick = state::onCreateRoom,
                )
            }
        },
    ) { modifier ->
        if (state.rooms.isEmpty()) {
            ProjectEmptyState(
                onCreate = state::onCreateRoom,
                onBrowsePlans = state::onShowPlans,
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { ProjectHeader(state) }
                items(state.rooms, key = { it.room.id }) { item ->
                    SwipeableRoomCard(
                        item = item,
                        onClick = { state.onOpenRoom(item.room.id) },
                        onDelete = { state.onDeleteRoom(item.room) },
                    )
                }
                item { Spacer(Modifier.height(MainBottomInset)) }
            }
        }
    }

    if (state.showCredits) {
        ModelCreditsDialog(onDismiss = state::onDismissCredits)
    }

    if (state.showPlans) {
        SamplePlanSheet(onPick = state::onCreateFromPlan, onDismiss = state::onDismissPlans)
    }
}

@Composable
private fun ProjectHeader(state: ProjectState) {
    CenterRow(
        Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.tab_project),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                pluralStringResource(R.plurals.room_count, state.rooms.size, state.rooms.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GlassIconButton(
            icon = Icons.Outlined.Dashboard,
            contentDescription = stringResource(R.string.sample_plans),
            onClick = state::onShowPlans,
        )
    }
}
