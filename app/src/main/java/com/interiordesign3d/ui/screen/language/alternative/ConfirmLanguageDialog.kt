package com.interiordesign3d.ui.screen.language.alternative

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.CenterRow

/** Tapping an already-picked language again on the Alt step lands here instead of applying it. */
@Composable
fun ConfirmLanguageDialog(isShown: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (!isShown) return

    // Dialogs open their own window; re-provide the context so stringResource follows the app locale.
    val localizedContext = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalContext provides localizedContext) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 12.dp,
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        stringResource(R.string.confirm_language_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(R.string.confirm_language_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    CenterRow(Modifier, Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = onConfirm) { Text(stringResource(R.string.apply)) }
                    }
                }
            }
        }
    }
}
