package com.interiordesign3d.ui.screen.onboard.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.onboard.OnboardSlot
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow

/**
 * One pager over whatever [slots] resolves to — real intro pages today, an ad slot wherever the
 * config puts one later. Skip is always reachable: an intro nobody can leave is a toll gate.
 */
@Composable
fun OnboardContent(
    slots: List<OnboardSlot>,
    onPageShown: (String) -> Unit,
    onLastPageShown: () -> Unit,
    onFinished: () -> Unit,
) {
    if (slots.isEmpty()) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    val pager = rememberPagerState { slots.size }
    val scope = rememberCoroutineScope()
    val lastIndex = slots.lastIndex
    val last = pager.currentPage == lastIndex

    LaunchedEffect(pager, slots) {
        snapshotFlow { pager.settledPage }.collectLatest { index ->
            slots.getOrNull(index)?.let { onPageShown(it.trackingName) }
            if (index == lastIndex) onLastPageShown()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text(
            stringResource(R.string.skip),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.End)
                .onClickNotRipple(onClick = onFinished)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        )

        HorizontalPager(pager, Modifier.weight(1f)) { index ->
            when (val slot = slots[index]) {
                is OnboardSlot.Page -> OnboardIntroContent(slot)
                is OnboardSlot.Ad -> OnboardAdContent(slot)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(slots.size) { i -> Dot(active = i == pager.currentPage) }
        }

        Spacer(Modifier.height(20.dp))

        GlassPillButton(
            icon = Icons.AutoMirrored.Outlined.ArrowForward,
            label = stringResource(if (last) R.string.ob_start else R.string.ob_next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            onClick = {
                if (last) onFinished()
                else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
        )
    }
}

@Composable
private fun Dot(active: Boolean) {
    val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "dotWidth")
    val color by animateColorAsState(
        if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "dotColor",
    )
    Box(Modifier.size(width = width, height = 8.dp).background(color, CircleShape))
}
