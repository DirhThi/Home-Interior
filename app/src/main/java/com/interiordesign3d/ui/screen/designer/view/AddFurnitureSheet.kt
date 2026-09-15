package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.catalog.CatalogItem
import com.interiordesign3d.data.catalog.FURNITURE_CATALOG
import com.interiordesign3d.ui.properties.fadeTrailingEdge
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.ChoiceChip
import com.interiordesign3d.ui.properties.AssetImage
import com.interiordesign3d.ui.properties.onClickNotRipple

/**
 * Two rows that scroll sideways instead of a tall vertical grid — the sheet stays about a
 * third of the screen so the room behind it is still visible while browsing.
 */
@Composable
fun AddFurnitureSheet(onAdd: (String, Boolean) -> Unit, onDismiss: () -> Unit) {
    var groupIdx by remember { mutableIntStateOf(0) }
    val group = FURNITURE_CATALOG[groupIdx]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.add_furniture),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            CenterRow(
                Modifier
                    .fillMaxWidth()
                    .fadeTrailingEdge()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                Arrangement.spacedBy(6.dp),
            ) {
                FURNITURE_CATALOG.forEachIndexed { index, catalogGroup ->
                    ChoiceChip(catalogGroup.title, index == groupIdx) { groupIdx = index }
                }
            }

            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(184.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(group.items, key = { it.key }) { item ->
                    FurnitureTile(item = item, onClick = { onAdd(item.key, item.wallMounted) })
                }
            }
        }
    }
}

@Composable
private fun FurnitureTile(item: CatalogItem, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.width(92.dp).onClickNotRipple(onClick = onClick),
    ) {
        Column(
            Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                AssetImage(item.preview, Modifier.fillMaxWidth(), item.label)
                if (item.wallMounted) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Text(
                            stringResource(R.string.wall_badge),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Text(
                item.label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
