package com.interiordesign3d.common.base

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
open class BaseScreenState {
    var loading by mutableStateOf(false)
}
