package com.interiordesign3d.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * App-wide settings. SharedPreferences rather than DataStore — DataStore was declared once and
 * referenced by nothing, and was removed with the rest of the unused dependencies.
 *
 * The value is held in Compose state so the whole tree recomposes the moment it changes; it is read
 * off disk once, in [InteriorDesignApp][com.interiordesign3d.InteriorDesignApp].
 */
object AppPrefs {
    private const val FILE = "app_prefs"
    private const val KEY_THEME = "theme_mode"

    private var prefs: SharedPreferences? = null

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init(context: Context) {
        val store = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        prefs = store
        themeMode = store.getString(KEY_THEME, null)
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.SYSTEM
    }

    fun updateTheme(mode: ThemeMode) {
        themeMode = mode
        prefs?.edit()?.putString(KEY_THEME, mode.name)?.apply()
    }
}
