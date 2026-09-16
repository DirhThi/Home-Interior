package com.interiordesign3d.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * The app's language, applied through `AppCompatDelegate` so the platform per-app-language API is
 * used on 33+ and backported below it — `resources.updateConfiguration` is deprecated and does not
 * survive a process restart.
 *
 * The codes match the set A045 ships. **The strings themselves are not translated yet**: picking a
 * language changes only what the system supplies until `res/values-<code>/` exists.
 */
object LanguageManager {

    val codes = listOf("en", "de", "es", "fr", "hi", "id", "it", "ja", "pt", "ru", "tr", "uk", "vi")

    var current by mutableStateOf(systemTag())
        private set

    fun apply(code: String) {
        if (code !in codes) return
        current = code
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }

    fun sync() {
        current = AppCompatDelegate.getApplicationLocales()
            .takeIf { !it.isEmpty }
            ?.get(0)
            ?.language
            ?.takeIf { it in codes }
            ?: systemTag()
    }

    /** Endonym — a language list is read by people who do not yet read the current one. */
    fun nativeName(code: String): String {
        val locale = Locale.forLanguageTag(code)
        return locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercase(locale) }
    }

    fun englishName(code: String): String =
        Locale.forLanguageTag(code).getDisplayLanguage(Locale.ENGLISH)

    private fun systemTag(): String {
        val lang = Locale.getDefault().language
        return codes.firstOrNull { it == lang } ?: "en"
    }
}
