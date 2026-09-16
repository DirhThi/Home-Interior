package com.interiordesign3d.ui.screen.onboard.view

import androidx.annotation.StringRes
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
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.properties.onClickNotRipple
import com.interiordesign3d.ui.screen.onboard.state.OnboardState
import kotlinx.coroutines.launch

private data class Page(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
)

private val PAGES = listOf(
    Page(Icons.Outlined.Architecture, R.string.ob1_title, R.string.ob1_body),
    Page(Icons.Outlined.Chair, R.string.ob2_title, R.string.ob2_body),
    Page(Icons.Outlined.Home, R.string.ob3_title, R.string.ob3_body),
)

/** Three pages, shown once. Skip is always reachable — an intro nobody can leave is a toll gate. */
@Composable
fun OnboardContent(state: OnboardState) {
    val pager = rememberPagerState { PAGES.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == PAGES.lastIndex

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
                .onClickNotRipple(onClick = state::onFinish)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        )

        HorizontalPager(pager, Modifier.weight(1f)) { index ->
            PageBody(PAGES[index])
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(PAGES.size) { i -> Dot(active = i == pager.currentPage) }
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
                if (last) state.onFinish()
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

@Composable
private fun PageBody(page: Page) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CenterBox(
            Modifier.size(140.dp).background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape
            )
        ) {
            Icon(
                page.icon,
                null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(36.dp))
        Text(
            stringResource(page.title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(page.body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
