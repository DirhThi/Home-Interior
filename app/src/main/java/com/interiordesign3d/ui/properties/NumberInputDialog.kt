package com.interiordesign3d.ui.properties

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.interiordesign3d.R
import kotlin.math.roundToInt

/**
 * Types an exact value for a slider.
 *
 * The text starts fully selected so the first keystroke replaces the current value instead of
 * appending to it, and an outside tap does not dismiss — losing a half-typed number to a stray
 * touch was worse than needing an explicit Cancel.
 */
@Composable
fun NumberInputDialog(
    title: String,
    suffix: String,
    initial: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float = 1f,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    // Dialogs open their own window; re-provide the context so stringResource follows the app locale.
    val localizedContext = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val start = initial.roundToInt().toString()
    var field by remember {
        mutableStateOf(TextFieldValue(start, selection = TextRange(0, start.length)))
    }
    val parsed = field.text.toFloatOrNull()

    fun nudge(delta: Float) {
        val next = ((parsed ?: initial) + delta).coerceIn(range.start, range.endInclusive)
        val t = next.roundToInt().toString()
        field = TextFieldValue(t, selection = TextRange(t.length))
    }

    // Reads the state at click time. Capturing `parsed` here meant a stale closure could
    // re-apply the value the dialog opened with, so typing appeared to do nothing.
    fun commit() {
        field.text.toFloatOrNull()
            ?.let { onConfirm(it.coerceIn(range.start, range.endInclusive)) }
        onDismiss()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        CompositionLocalProvider(LocalContext provides localizedContext) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                shadowElevation = 12.dp,
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium)

                    CenterRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        StepButton(Icons.Outlined.Remove, R.string.decrease) { nudge(-step) }

                        ValueField(
                            field = field,
                            onChange = { input ->
                                field = input.copy(
                                    text = input.text.filter { it.isDigit() }.take(4),
                                )
                            },
                            suffix = suffix,
                            isError = parsed == null,
                            focusRequester = focusRequester,
                            onDone = { commit() },
                            modifier = Modifier.weight(1f),
                        )

                        StepButton(Icons.Outlined.Add, R.string.increase) { nudge(step) }
                    }

                    Text(
                        stringResource(
                            R.string.value_range,
                            range.start.roundToInt(),
                            range.endInclusive.roundToInt(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    CenterRow(Modifier.fillMaxWidth(), Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { commit() }, enabled = parsed != null) {
                            Text(stringResource(R.string.apply))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: Int,
    onClick: () -> Unit,
) {
    PanelIconButton(icon, stringResource(contentDescription), onClick = onClick)
}

@Composable
private fun ValueField(
    field: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    suffix: String,
    isError: Boolean,
    focusRequester: FocusRequester,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Box(
        modifier
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(if (isError) 2.dp else 1.dp, accent, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        CenterRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicTextField(
                value = field,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(accent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier.width(72.dp).focusRequester(focusRequester),
            )
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                Text(suffix, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
