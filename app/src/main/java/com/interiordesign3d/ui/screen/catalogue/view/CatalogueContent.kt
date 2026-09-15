package com.interiordesign3d.ui.screen.catalogue.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseScreen
import com.interiordesign3d.data.catalog.CatalogItem
import com.interiordesign3d.data.catalog.FURNITURE_CATALOG
import com.interiordesign3d.ui.properties.AssetImage
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.ChoiceChip
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.properties.fadeTrailingEdge
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.catalogue.state.CatalogueState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

/** Browse every model in the catalogue on its own, without a room open. */
@Composable
fun CatalogueContent(state: CatalogueState) {
    val group = FURNITURE_CATALOG[state.groupIdx.coerceIn(FURNITURE_CATALOG.indices)]

    BaseScreen(loading = state.loading) { modifier ->
        Column(modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.catalogue),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            )

            CenterRow(
                Modifier
                    .fillMaxWidth()
                    .fadeTrailingEdge()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                Arrangement.spacedBy(6.dp),
            ) {
                FURNITURE_CATALOG.forEachIndexed { index, catalogGroup ->
                    ChoiceChip(catalogGroup.title, index == state.groupIdx) {
                        state.onSelectGroup(index)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(112.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 108.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(group.items, key = { it.key }) { item ->
                    CatalogueTile(item = item, onClick = { state.onOpenItem(item.key) })
                }
            }
        }
    }
}

@Composable
private fun CatalogueTile(item: CatalogItem, onClick: () -> Unit) {
    GlassPane(
        modifier = Modifier.fillMaxWidth().onClickNotRipple(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        strong = true,
        elevation = 4.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AssetImage(
                path = item.preview,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
            Text(
                item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
