package com.interiordesign3d.ui.properties

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.interiordesign3d.R
import kotlin.math.roundToInt

/**
 * Types an exact value for a slider. Clamps to the slider's range rather than rejecting,
 * so a typed 500 on a 0..360 rotation lands on 360 instead of doing nothing.
 */
@Composable
fun NumberInputDialog(
    title: String,
    suffix: String,
    initial: Float,
    range: ClosedFloatingPointRange<Float>,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    // Dialogs open their own window; re-provide the context so stringResource follows the app locale.
    val localizedContext = LocalContext.current
    var text by remember { mutableStateOf(initial.roundToInt().toString()) }
    val parsed = text.toFloatOrNull()

    fun commit() {
        parsed?.let { onConfirm(it.coerceIn(range.start, range.endInclusive)) }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input -> text = input.filter { it.isDigit() || it == '-' }.take(5) },
                    singleLine = true,
                    isError = parsed == null,
                    suffix = { Text(suffix) },
                    supportingText = {
                        Text(
                            stringResource(
                                R.string.value_range,
                                range.start.roundToInt(),
                                range.endInclusive.roundToInt(),
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = ::commit, enabled = parsed != null) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
