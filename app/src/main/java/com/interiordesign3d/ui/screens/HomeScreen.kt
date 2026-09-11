package com.interiordesign3d.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.interiordesign3d.data.models.DesignRoom
import com.interiordesign3d.data.repository.AppDatabase
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToDesigner: (String) -> Unit) {
    val context = LocalContext.current
    val db      = remember { AppDatabase.getInstance(context) }
    val scope   = rememberCoroutineScope()
    val rooms   by db.roomDao().getAllRooms().collectAsState(initial = emptyList())
    var showCredits by remember { mutableStateOf(false) }

    if (showCredits) {
        val localizedContext = context
        AlertDialog(
            onDismissRequest = { showCredits = false },
            confirmButton = { TextButton(onClick = { showCredits = false }) { Text("Đóng") } },
            title = { Text("Nguồn mô hình 3D") },
            text = {
                CompositionLocalProvider(LocalContext provides localizedContext) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MODEL_CREDITS) { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Budget Interiors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text("${rooms.size} room${if (rooms.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { showCredits = true }) { Icon(Icons.Filled.Info, "Nguồn mô hình") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val newId = UUID.randomUUID().toString()
                    scope.launch {
                        db.roomDao().insertRoom(
                            DesignRoom(
                                id        = newId,
                                name      = "New Room",
                                widthCm   = 380f,
                                lengthCm  = 520f,
                                heightCm  = 260f,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    onNavigateToDesigner(newId)
                },
                icon           = { Icon(Icons.Filled.Add, null) },
                text           = { Text("New Room") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        if (rooms.isEmpty()) {
            HomeEmptyState(Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(padding),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rooms, key = { it.id }) { room ->
                    SwipeableRoomItem(
                        room     = room,
                        onClick  = { onNavigateToDesigner(room.id) },
                        onDelete = {
                            scope.launch {
                                db.placedFurnitureDao().clearRoomFurniture(room.id)
                                db.roomDao().deleteRoom(room)
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun HomeEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Weekend, null,
            modifier = Modifier.size(72.dp),
            tint     = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text("No rooms yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Tap + to design your first room",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Swipeable room list item ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableRoomItem(
    room: DesignRoom,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == EndToStart) { onDelete(); true } else false
        }
    )

    SwipeToDismissBox(
        state             = state,
        backgroundContent = {
            val color = if (state.dismissDirection == EndToStart)
                MaterialTheme.colorScheme.errorContainer
            else Color.Transparent
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (state.dismissDirection == EndToStart)
                    Icon(Icons.Filled.Delete, null,
                        tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        RoomListItem(room = room, onClick = onClick)
    }
}

// ─── Room list item ───────────────────────────────────────────────────────────

@Composable
private fun RoomListItem(room: DesignRoom, onClick: () -> Unit) {
    val wallColor = try {
        Color(android.graphics.Color.parseColor(room.wallColor))
    } catch (e: Exception) {
        Color(0xFFF5F0EB)
    }

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(wallColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Weekend, null,
                    tint     = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(room.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${room.widthCm.toInt()} × ${room.lengthCm.toInt()} cm  ·  h ${room.heightCm.toInt()} cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick  = {},
                    label    = { Text(room.floorMaterial.displayName,
                        style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(22.dp)
                )
            }

            Icon(Icons.Filled.ChevronRight, null,
                tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

