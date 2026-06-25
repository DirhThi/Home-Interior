package com.interiordesign3d.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import com.interiordesign3d.data.repository.ColorPaletteRepository

// ═══════════════════════════════════════════════════════════════════════════════
// COLOR PICKER SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerScreen(roomId: String, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedWallColor by remember { mutableStateOf("#F5F0EB") }
    var selectedFloor by remember { mutableStateOf(FloorMaterial.HARDWOOD) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Colors & Materials") },
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    Button(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Apply")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Wall Colors", "Floor", "Palettes").forEachIndexed { idx, title ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx },
                        text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> WallColorTab(selectedColor = selectedWallColor,
                    onColorSelect = { selectedWallColor = it })
                1 -> FloorMaterialTab(selectedMaterial = selectedFloor,
                    onSelect = { selectedFloor = it })
                2 -> PaletteTab()
            }
        }
    }
}

@Composable
private fun WallColorTab(selectedColor: String, onColorSelect: (String) -> Unit) {
    val paintColors = listOf(
        // Whites & Neutrals
        "#FFFFFF","#F5F0EB","#F0EBE3","#EDE0D0","#E8D5C4",
        // Warm tones
        "#F2C4A0","#E8A87C","#D4785A","#B5451B","#8A3210",
        // Greens
        "#E8F5E9","#C8E6C9","#81C784","#4A7C59","#2C5F3E",
        // Blues
        "#E3F2FD","#90CAF9","#42A5F5","#1565C0","#0D47A1",
        // Grays
        "#F5F5F5","#E0E0E0","#9E9E9E","#616161","#212121",
        // Accent
        "#FFF9C4","#FFE082","#FFB300","#FF7043","#F4511E"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(16.dp),
            color = try { Color(android.graphics.Color.parseColor(selectedColor)) }
                    catch (e: Exception) { Color(0xFFF5F0EB) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Wall Preview", color = Color.Black.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }

        Text("Paint Colors", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold)

        LazyVerticalGrid(columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(paintColors) { hex ->
                val isSelected = hex == selectedColor
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(try { Color(android.graphics.Color.parseColor(hex)) }
                                    catch (e: Exception) { Color.Gray })
                        .border(if (isSelected) 3.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.15f),
                            CircleShape)
                        .clickable { onColorSelect(hex) }
                )
            }
        }
    }
}

@Composable
private fun FloorMaterialTab(selectedMaterial: FloorMaterial, onSelect: (FloorMaterial) -> Unit) {
    val materialEmojis = mapOf(
        FloorMaterial.HARDWOOD to "🪵", FloorMaterial.MARBLE to "⬜",
        FloorMaterial.TILE to "🔲", FloorMaterial.CARPET to "🟫",
        FloorMaterial.CONCRETE to "🩶", FloorMaterial.LAMINATE to "📋"
    )

    LazyVerticalGrid(columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(FloorMaterial.entries) { material ->
            val isSelected = material == selectedMaterial
            Surface(
                onClick = { onSelect(material) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
            ) {
                Row(modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(materialEmojis[material] ?: "🏠", fontSize = 32.sp)
                    Column {
                        Text(material.displayName, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        if (isSelected) {
                            Text("Selected ✓", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteTab() {
    LazyColumn(contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(ColorPaletteRepository.palettes) { palette ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(palette.name, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text(palette.style.displayName, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {}) { Text("Apply") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(palette.primary, palette.secondary, palette.accent, palette.background)
                            .forEach { hex ->
                                Box(modifier = Modifier.weight(1f).height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(try { Color(android.graphics.Color.parseColor(hex)) }
                                                catch (e: Exception) { Color.Gray }))
                            }
                    }
                }
            }
        }
    }
}
