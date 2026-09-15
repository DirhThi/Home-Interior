package com.interiordesign3d.common.utils

import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.ui.navigation.Dest

/**
 * Back-stack edits. Every one of them refuses to empty the stack — `NavDisplay` crashes on an empty
 * back stack, so "keep at least one entry" is a rule, not a nicety.
 */
object NavigationUtil {

    /** Pushes unless the same destination is already on top, which is what a double tap produces. */
    fun <T : NavKey> MutableList<T>.navigateTo(dest: T, popupTos: List<Class<out T>> = listOf()) {
        if (lastOrNull() == dest) return
        add(dest)
        if (popupTos.isNotEmpty()) {
            removeIf { it !== dest && it::class.java in popupTos }
        }
    }

    fun MutableList<NavKey>.popLast(): Boolean {
        if (size <= 1) return false
        removeAt(lastIndex)
        return true
    }

    fun MutableList<NavKey>.popToFirst(): Boolean {
        if (size <= 1) return false
        while (size > 1) removeAt(lastIndex)
        return true
    }

    /** Leaves whatever flow is open and lands back on the shell, keeping the tab it was showing. */
    fun MutableList<NavKey>.popToMain(): Boolean {
        val mainIndex = indexOfLast { it is Dest.ScrMain }
        if (mainIndex < 0) return popToFirst()
        if (mainIndex == lastIndex) return false
        while (lastIndex > mainIndex) removeAt(lastIndex)
        return true
    }
}
