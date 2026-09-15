package com.interiordesign3d.ui.screen.catalogue.item.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.GlassIconButton
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.catalogue.item.state.CatalogueItemState
import com.interiordesign3d.ui.theme.LocalGlass
import com.interiordesign3d.ui.theme.LocalInteriorAccents

/** One model, on its own, turntable-style — the room is not in the way here. */
@Composable
fun CatalogueItemContent(state: CatalogueItemState) {
    val item = state.item
    val accents = LocalInteriorAccents.current

    Box(Modifier.fillMaxSize()) {
        if (item != null) {
            FilamentModelPreview(
                modelPath = item.model,
                background = accents.viewportBackground,
                modifier = Modifier.fillMaxSize(),
            )
        }

        GlassIconButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.back),
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp),
            onClick = state::onBack,
        )

        if (item != null) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GlassPane(shape = MaterialTheme.shapes.large, strong = true, elevation = 10.dp) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
                GlassPillButton(
                    icon = Icons.Outlined.Add,
                    label = stringResource(R.string.add_to_room),
                    onClick = state::onAddTapped,
                )
            }
        }
    }

    if (state.showRoomPicker) {
        RoomPickerSheet(state)
    }
}

@Composable
private fun RoomPickerSheet(state: CatalogueItemState) {
    val glass = LocalGlass.current

    ModalBottomSheet(
        onDismissRequest = state::onDismissRoomPicker,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.hasRooms) {
                Text(stringResource(R.string.choose_room), style = MaterialTheme.typography.titleLarge)
                state.rooms.forEach { room ->
                    CenterRow(
                        Modifier
                            .fillMaxWidth()
                            .onClickNotRipple { state.onAddToRoom(room.id) }
                            .padding(vertical = 14.dp),
                    ) {
                        Text(room.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                Text(stringResource(R.string.no_rooms_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.no_rooms_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GlassPillButton(
                    icon = Icons.Outlined.Add,
                    label = stringResource(R.string.no_rooms_cta),
                    modifier = Modifier.padding(top = 6.dp),
                    onClick = state::onCreateRoom,
                )
            }
        }
    }
}
