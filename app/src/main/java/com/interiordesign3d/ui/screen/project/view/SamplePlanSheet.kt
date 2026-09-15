package com.interiordesign3d.ui.screen.project.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.models.FloorPlan
import com.interiordesign3d.data.plans.SAMPLE_PLANS
import com.interiordesign3d.data.plans.SamplePlan
import com.interiordesign3d.data.plans.areaM2
import com.interiordesign3d.data.plans.readSamplePlan
import com.interiordesign3d.ui.properties.onClickNotRipple

@Composable
fun SamplePlanSheet(onPick: (SamplePlan) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val plans = remember {
        SAMPLE_PLANS.mapNotNull { plan -> readSamplePlan(context, plan)?.let { plan to it } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(R.string.sample_plans),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Text(
                stringResource(R.string.sample_plans_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(plans, key = { it.first.key }) { (plan, floorPlan) ->
                    PlanCard(plan = plan, floorPlan = floorPlan, onClick = { onPick(plan) })
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: SamplePlan, floorPlan: FloorPlan, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.onClickNotRipple(onClick = onClick),
    ) {
        Column(
            Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PlanThumbnail(floorPlan, Modifier.fillMaxWidth().height(112.dp))
            Text(plan.label, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
                pluralStringResource(
                    R.plurals.plan_rooms_area,
                    floorPlan.rooms.size,
                    floorPlan.rooms.size,
                    floorPlan.areaM2(),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
