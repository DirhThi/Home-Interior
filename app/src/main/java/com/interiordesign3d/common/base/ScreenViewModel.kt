package com.interiordesign3d.common.base

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.material3.SnackbarHostState

/** Stands in for DI: builds the screen's ViewModel with the Application and this screen's Navigator. */
@Composable
inline fun <reified VM : ViewModel> rememberScreenViewModel(
    key: String? = null,
    crossinline create: (Application) -> VM,
): VM {
    val app = LocalContext.current.applicationContext as Application
    return viewModel(key = key, factory = viewModelFactory { initializer { create(app) } })
}

/** Drains the ViewModel's one-shot messages into the screen's snackbar. */
@Composable
fun CollectMessages(viewModel: BaseViewModel, host: SnackbarHostState) {
    LaunchedEffect(viewModel, host) {
        viewModel.messages.collect { host.showSnackbar(it.text) }
    }
}
