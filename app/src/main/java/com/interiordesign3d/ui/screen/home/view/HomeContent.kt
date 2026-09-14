package com.interiordesign3d.ui.screen.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseScreen
import com.interiordesign3d.ui.screen.home.state.HomeState

@Composable
fun HomeContent(
    state: HomeState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    BaseScreen(
        loading = state.loading,
        snackbarHostState = snackbarHostState,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            pluralStringResource(R.plurals.room_count, state.rooms.size, state.rooms.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = state::onShowCredits) {
                        Icon(Icons.Outlined.Info, stringResource(R.string.model_credits))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            if (state.rooms.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = state::onCreateRoom,
                    icon = { Icon(Icons.Outlined.Add, null) },
                    text = { Text(stringResource(R.string.new_room)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { modifier ->
        if (state.rooms.isEmpty()) {
            HomeEmptyState(onCreate = state::onCreateRoom, modifier = modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.rooms, key = { it.id }) { room ->
                    SwipeableRoomCard(
                        room = room,
                        onClick = { state.onOpenRoom(room.id) },
                        onDelete = { state.onDeleteRoom(room) },
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }

    if (state.showCredits) {
        ModelCreditsDialog(onDismiss = state::onDismissCredits)
    }
}
