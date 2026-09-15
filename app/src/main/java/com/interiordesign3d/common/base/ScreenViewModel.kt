package com.interiordesign3d.common.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/** Drains the ViewModel's one-shot messages into the screen's snackbar. */
@Composable
fun CollectMessages(viewModel: BaseViewModel, host: SnackbarHostState) {
    LaunchedEffect(viewModel, host) {
        viewModel.messages.collect { msg ->
            val result = host.showSnackbar(
                message = msg.text,
                actionLabel = msg.actionLabel,
                withDismissAction = msg.actionLabel == null,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) msg.onAction?.invoke()
        }
    }
}
