package com.interiordesign3d.ui.screen.home.view

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.parseHexColor
import com.interiordesign3d.ui.properties.rounded

@Composable
fun SwipeableRoomCard(room: DesignRoom, onClick: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        },
        positionalThreshold = { width -> width * 0.4f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .rounded(14.dp)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        RoomCard(room = room, onClick = onClick)
    }
}

@Composable
private fun RoomCard(room: DesignRoom, onClick: () -> Unit) {
    val wallColor = parseHexColor(room.wallColor, MaterialTheme.colorScheme.surfaceContainerHighest)
    val floorColor = parseHexColor(room.floorColor, MaterialTheme.colorScheme.tertiaryContainer)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        CenterRow(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RoomThumbnail(wallColor = wallColor, floorColor = floorColor)

            Column(Modifier.weight(1f)) {
                Text(
                    room.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(
                        R.string.room_size,
                        room.widthCm.toInt(),
                        room.lengthCm.toInt(),
                        room.heightCm.toInt(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    room.floorMaterial.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Icon(
                Icons.Outlined.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/** Wall colour as the tile, floor colour as the band underneath — a glanceable stand-in for a render. */
@Composable
private fun RoomThumbnail(wallColor: Color, floorColor: Color) {
    Box(
        Modifier
            .size(64.dp)
            .rounded(12.dp)
            .background(wallColor)
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(18.dp)
                .background(floorColor)
        )
        CenterBox(Modifier.fillMaxSize()) {
            Icon(
                Icons.Outlined.Weekend,
                null,
                Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f),
            )
        }
    }
}
