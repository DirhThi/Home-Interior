package com.interiordesign3d.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import kotlin.math.*

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
    onAdd: (FurnitureCategory, Float, Float, Float, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCat by remember { mutableStateOf(FurnitureCategory.SOFA) }
    var wallMount   by remember { mutableStateOf(false) }
    var scale       by remember { mutableStateOf(1f) }

    val (baseW, baseD, baseH) = CATEGORY_DEFAULTS[selectedCat] ?: Triple(60f, 60f, 60f)
    var wCm by remember(selectedCat) { mutableStateOf(baseW) }
    var dCm by remember(selectedCat) { mutableStateOf(baseD) }
    var hCm by remember(selectedCat) { mutableStateOf(baseH) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: icon + name
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedCat.icon, style = MaterialTheme.typography.titleLarge)
                    Text(selectedCat.displayName, style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

            // Category picker
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FurnitureCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { selectedCat = cat; scale = 1f },
                        label = { Text("${cat.icon} ${cat.displayName}",
                            style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // 3D preview
            Surface(modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(16.dp), color = Color(0xFF1A1A2E)) {
                val s = scale; val pWall = wallMount
                Canvas(Modifier.fillMaxSize()) {
                    if (pWall) {
                        val baseSc = size.minDimension / maxOf(wCm, hCm).coerceAtLeast(1f) * 0.35f
                        val cx = size.width/2f; val cy = size.height/2f
                        val hw = wCm*s/2f*baseSc; val hh = hCm*s/2f*baseSc; val bev = 8f
                        val color = Color(0xFF8B7355)
                        drawPath(Path().apply {
                            moveTo(cx-hw,cy-hh); lineTo(cx+hw,cy-hh)
                            lineTo(cx+hw+bev,cy-hh-bev*.5f); lineTo(cx-hw+bev,cy-hh-bev*.5f); close()
                        }, color * 1.25f)
                        drawPath(Path().apply {
                            moveTo(cx+hw,cy-hh); lineTo(cx+hw,cy+hh)
                            lineTo(cx+hw+bev,cy+hh-bev*.5f); lineTo(cx+hw+bev,cy-hh-bev*.5f); close()
                        }, color * 0.72f)
                        drawRect(color, Offset(cx-hw,cy-hh), Size(hw*2,hh*2))
                        drawRect(Color.Black.copy(alpha=0.18f), Offset(cx-hw,cy-hh), Size(hw*2,hh*2), style=Stroke(1.5f))
                    } else {
                        val baseSc = size.minDimension / maxOf(wCm, dCm, hCm).coerceAtLeast(1f) * 0.30f
                        val azRad = Math.toRadians(30.0); val elRad = Math.toRadians(35.0)
                        val cx = size.width/2f; val cy = size.height*0.56f
                        fun proj(x: Float, y: Float, z: Float) = Offset(
                            cx + ((x*cos(azRad)+z*sin(azRad))*baseSc).toFloat(),
                            cy - (((x*sin(azRad)-z*cos(azRad))*sin(elRad)+y*cos(elRad))*baseSc).toFloat()
                        )
                        val hw = wCm*s/2f; val hd = dCm*s/2f; val bh = hCm*s
                        val color = Color(0xFF8B7355)
                        fun quad(x1:Float,y1:Float,z1:Float,x2:Float,y2:Float,z2:Float,
                                 x3:Float,y3:Float,z3:Float,x4:Float,y4:Float,z4:Float) = Path().apply {
                            val a=proj(x1,y1,z1);val b=proj(x2,y2,z2);val c=proj(x3,y3,z3);val d=proj(x4,y4,z4)
                            moveTo(a.x,a.y);lineTo(b.x,b.y);lineTo(c.x,c.y);lineTo(d.x,d.y);close()
                        }
                        drawPath(quad(-hw,bh,-hd,hw,bh,-hd,hw,bh,hd,-hw,bh,hd), color)
                        drawPath(quad(-hw,bh,hd,hw,bh,hd,hw,0f,hd,-hw,0f,hd), color*0.82f)
                        drawPath(quad(hw,bh,-hd,hw,bh,hd,hw,0f,hd,hw,0f,-hd), color*0.65f)
                    }
                }
            }

            // Scale
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Scale", style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text("${(scale*100).toInt()}%", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = scale, onValueChange = { scale = it },
                    valueRange = 1f..2f, modifier = Modifier.fillMaxWidth())
            }

            HorizontalDivider()

            // Dim sliders: thumb = base value, label = base × scale
            DimSlider("Width",  wCm, 20f..1000f, displayValue = wCm * scale) { wCm = it }
            if (!wallMount) DimSlider("Depth", dCm, 20f..1000f, displayValue = dCm * scale) { dCm = it }
            DimSlider("Height", hCm, 10f..600f,  displayValue = hCm * scale) { hCm = it }

            // Wall mount
            Surface(shape = RoundedCornerShape(10.dp),
                color = if (wallMount) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(horizontal=14.dp, vertical=10.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp), verticalAlignment=Alignment.CenterVertically) {
                        Icon(Icons.Filled.Tv, null, Modifier.size(18.dp),
                            tint = if (wallMount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Wall Mount", style=MaterialTheme.typography.labelMedium,
                            fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    Switch(checked=wallMount, onCheckedChange={ wallMount=it })
                }
            }

            Button(onClick = { onAdd(selectedCat, wCm * scale, dCm * scale, hCm * scale, wallMount) },
                modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.AddCircle, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add to Room")
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
