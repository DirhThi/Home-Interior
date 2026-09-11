package com.interiordesign3d.ui.screens

import android.graphics.BitmapFactory
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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import kotlin.math.*

// ─── Asset image (loads a png from assets/ — used for Kenney furniture previews) ──

@Composable
internal fun AssetImage(path: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val img = remember(path) {
        runCatching { ctx.assets.open(path).use { BitmapFactory.decodeStream(it) }.asImageBitmap() }.getOrNull()
    }
    if (img != null) Image(img, null, modifier, contentScale = ContentScale.Fit)
    else Box(modifier.background(Color(0x11000000), RoundedCornerShape(8.dp)))
}

// ─── Furniture category default dimensions ────────────────────────────────────

internal val CATEGORY_DEFAULTS = mapOf(
    FurnitureCategory.SOFA      to Triple(200f,  85f,  75f),
    FurnitureCategory.CHAIR     to Triple(60f,   60f,  85f),
    FurnitureCategory.TABLE     to Triple(120f,  80f,  75f),
    FurnitureCategory.BED       to Triple(160f, 200f,  50f),
    FurnitureCategory.WARDROBE  to Triple(120f,  60f, 200f),
    FurnitureCategory.BOOKSHELF to Triple(80f,   30f, 180f),
    FurnitureCategory.LAMP      to Triple(40f,   40f, 150f),
    FurnitureCategory.RUG       to Triple(200f, 150f,   2f),
    FurnitureCategory.PLANT     to Triple(40f,   40f, 100f),
    FurnitureCategory.DECOR     to Triple(50f,   50f,  50f)
)

// ─── Add Furniture Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddFurnitureSheet(
    onAdd: (String, Boolean) -> Unit,   // (itemKey, wallMounted)
    onDismiss: () -> Unit
) {
    var groupIdx by remember { mutableStateOf(0) }
    val group = FURNITURE_CATALOG[groupIdx]

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Thêm nội thất", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(FURNITURE_CATALOG) { i, g ->
                    FilterChip(
                        selected = i == groupIdx,
                        onClick = { groupIdx = i },
                        label = { Text(g.title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(group.items, key = { it.key }) { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { onAdd(item.key, item.wallMounted); onDismiss() }
                    ) {
                        Column(
                            Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                AssetImage(item.preview, Modifier.fillMaxWidth().height(72.dp))
                                if (item.wallMounted) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text("Tường", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(item.label, style = MaterialTheme.typography.labelSmall,
                                maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

// ─── Wall / floor surface picker ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SurfaceSheet(
    wallIdx: Int, floorIdx: Int, shadows: Boolean, autoHide: Boolean,
    onWall: (Int) -> Unit, onFloor: (Int) -> Unit, onShadows: (Boolean) -> Unit, onAutoHide: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Ẩn tường phía trước", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Tự ẩn 2 tường chắn tầm nhìn theo góc xoay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = autoHide, onCheckedChange = onAutoHide)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Đổ bóng", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(if (shadows) "Bật — đẹp hơn, nặng hơn" else "Tắt — mượt hơn",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = shadows, onCheckedChange = onShadows)
            }
            HorizontalDivider()
            Text("Tường", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(WALL_PRESETS) { i, p ->
                    FilterChip(selected = i == wallIdx, onClick = { onWall(i) },
                        label = { Text(p.label, style = MaterialTheme.typography.labelMedium) })
                }
            }
            Text("Sàn", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(FLOOR_PRESETS) { i, p ->
                    FilterChip(selected = i == floorIdx, onClick = { onFloor(i) },
                        label = { Text(p.label, style = MaterialTheme.typography.labelMedium) })
                }
            }
        }
    }
}

// ─── Shared dimension slider ──────────────────────────────────────────────────

@Composable
internal fun DimSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: Float? = null,
    enabled: Boolean = true,
    onChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${(displayValue ?: value).toInt()} cm",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onChanged, valueRange = range,
            enabled = enabled, modifier = Modifier.fillMaxWidth())
    }
}
