package com.interiordesign3d.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/** Back leaves instantly rather than running NavDisplay's default scale-and-fade. */
object NavTransition {

    /** No animation at all, either direction — a Base→Alt push the user shouldn't notice. */
    val none: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        ContentTransform(EnterTransition.None, ExitTransition.None)
    }

    val pop: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        ContentTransform(EnterTransition.None, ExitTransition.None)
    }

    val predictivePop: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = {
        ContentTransform(EnterTransition.None, ExitTransition.None)
    }
}
