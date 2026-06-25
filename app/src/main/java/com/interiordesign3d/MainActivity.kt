package com.interiordesign3d

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.interiordesign3d.ui.InteriorDesignNavHost
import com.interiordesign3d.ui.theme.InteriorDesignTheme

class InteriorDesignApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Init global singletons here (e.g. DB, DI, analytics)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkTheme = isSystemInDarkTheme()
            InteriorDesignTheme(darkTheme = darkTheme) {
                InteriorDesignNavHost()
            }
        }
    }
}
