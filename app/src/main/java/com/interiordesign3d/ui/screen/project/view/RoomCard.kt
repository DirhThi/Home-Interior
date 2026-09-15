package com.interiordesign3d.ui.screen.project.view

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.models.PlacedFurniture
import com.interiordesign3d.data.plans.areaM2
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.properties.rounded
import com.interiordesign3d.ui.screen.project.state.RoomListItem

@Composable
fun SwipeableRoomCard(item: RoomListItem, onClick: () -> Unit, onDelete: () -> Unit) {
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
            // Only while the card is actually moving: the card in front is glass, so a red slab
            // painted under a resting one tints it through.
            if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .rounded(20.dp)
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
            }
        },
    ) {
        RoomCard(item = item, onClick = onClick)
    }
}

@Composable
private fun RoomCard(item: RoomListItem, onClick: () -> Unit) {
    val plan = item.plan
    val drawn = plan.rooms.isNotEmpty()

    GlassPane(
        modifier = Modifier.fillMaxWidth().onClickNotRipple(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        strong = true,
        elevation = 6.dp,
    ) {
        CenterRow(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RoomThumbnail(plan = plan, furniture = item.furniture, drawn = drawn)

            Column(Modifier.weight(1f)) {
                Text(
                    item.room.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    if (drawn) pluralStringResource(
                        R.plurals.plan_rooms_area,
                        plan.rooms.size,
                        plan.rooms.size,
                        plan.areaM2(),
                    ) else stringResource(R.string.room_not_drawn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (drawn && plan.levelCount > 1) stringResource(
                        R.string.room_levels_ceiling,
                        plan.levelCount,
                        item.room.heightCm.toInt(),
                    ) else stringResource(R.string.room_ceiling, item.room.heightCm.toInt()),
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

/** The plan drawn to scale — a better stand-in for a render than a flat colour swatch was. */
@Composable
private fun RoomThumbnail(plan: FloorPlan, furniture: List<PlacedFurniture>, drawn: Boolean) {
    Box(
        Modifier
            .size(72.dp)
            .rounded(14.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (drawn) {
            PlanThumbnail(plan, Modifier.fillMaxSize(), furniture)
        } else {
            CenterBox(Modifier.fillMaxSize()) {
                Icon(
                    Icons.Outlined.Weekend,
                    null,
                    Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f),
                )
            }
        }
    }
}
