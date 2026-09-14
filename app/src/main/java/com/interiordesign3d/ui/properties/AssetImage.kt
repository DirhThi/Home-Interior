package com.interiordesign3d.ui.properties

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/** Decodes a WebP/PNG preview straight out of assets/; falls back to a neutral tile. */
@Composable
fun AssetImage(path: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val ctx = LocalContext.current
    val image = remember(path) {
        runCatching {
            ctx.assets.open(path).use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        }.getOrNull()
    }
    if (image != null) {
        Image(image, contentDescription, modifier, contentScale = ContentScale.Fit)
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.small))
    }
}
