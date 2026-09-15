package com.interiordesign3d

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.data.repository.ThemeMode
import com.interiordesign3d.di.viewModelModule
import com.interiordesign3d.ui.navigation.AppNavigation
import com.interiordesign3d.ui.theme.InteriorDesignTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InteriorDesignApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPrefs.init(this)
        startKoin {
            androidContext(this@InteriorDesignApp)
            modules(viewModelModule)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                AppNavigation()
            }
        }
    }
}
