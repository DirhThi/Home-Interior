package com.interiordesign3d.ui.screen.project.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.catalog.MODEL_CREDITS

@Composable
fun ModelCreditsDialog(onDismiss: () -> Unit) {
    // Dialogs open their own window; re-provide the context so stringResource follows the app locale.
    val localizedContext = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.model_credits)) },
        text = {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MODEL_CREDITS) {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}
