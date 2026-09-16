package com.interiordesign3d.ui.screen.onboard.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.interiordesign3d.data.onboard.OnboardType
import com.interiordesign3d.ui.properties.AdSlot
import com.interiordesign3d.ui.properties.CenterBox
import com.interiordesign3d.ui.screen.onboard.OnboardSlot

/** Icon per page — the enum itself stays in the data layer, free of UI types. */
private val OnboardType.icon: ImageVector
    get() = when (this) {
        OnboardType.Ob1 -> Icons.Outlined.Architecture
        OnboardType.Ob2 -> Icons.Outlined.Chair
        OnboardType.Ob3 -> Icons.Outlined.Home
    }

@Composable
fun OnboardIntroContent(slot: OnboardSlot.Page) {
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
                slot.type.icon,
                null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(36.dp))
        Text(
            stringResource(slot.type.title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(slot.type.body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        AdSlot(nameSpace = slot.placement, modifier = Modifier.fillMaxWidth())
    }
}
