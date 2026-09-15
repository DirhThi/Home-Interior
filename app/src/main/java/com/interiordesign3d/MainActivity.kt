package com.interiordesign3d

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.data.repository.ThemeMode
import com.interiordesign3d.ui.InteriorDesignNavHost
import com.interiordesign3d.ui.theme.InteriorDesignTheme

class InteriorDesignApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPrefs.init(this)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkTheme = when (AppPrefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            InteriorDesignTheme(darkTheme = darkTheme) {
                InteriorDesignNavHost()
            }
        }
    }
}
