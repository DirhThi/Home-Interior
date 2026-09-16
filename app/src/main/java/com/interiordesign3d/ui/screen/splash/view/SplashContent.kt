package com.interiordesign3d.ui.screen.splash.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.screen.splash.state.SplashState

@Composable
fun SplashContent(state: SplashState) {
    var shown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (shown) 1f else 0.88f, label = "splashMark")
    val alpha by animateFloatAsState(if (shown) 1f else 0f, label = "splashFade")

    LaunchedEffect(Unit) { shown = true }

    CenterBox(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.graphicsLayer {
                scaleX = scale; scaleY = scale; this.alpha = alpha
            },
        ) {
            CenterBox(
                Modifier.size(104.dp).background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape
                )
            ) {
                Icon(
                    Icons.Outlined.Weekend,
                    null,
                    Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
