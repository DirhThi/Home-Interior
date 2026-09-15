package com.interiordesign3d.common.base

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.utils.NavigationUtil.navigateTo
import com.interiordesign3d.common.utils.NavigationUtil.popLast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** One-shot text shown in the screen's snackbar. */
data class UiMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

open class BaseViewModel(
    protected val app: Application,
    private val backStack: NavBackStack<NavKey>,
) : ViewModel() {

    private val _messages = MutableSharedFlow<UiMessage>()
    val messages = _messages.asSharedFlow()

    protected fun navigateTo(dest: NavKey, popupTos: List<Class<out NavKey>> = listOf()) =
        backStack.navigateTo(dest, popupTos)

    protected fun pops() {
        backStack.popLast()
    }

    protected fun notify(@StringRes res: Int) = notify(app.getString(res))

    protected fun notify(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        viewModelScope.launch { _messages.emit(UiMessage(text, actionLabel, onAction)) }
    }
}
