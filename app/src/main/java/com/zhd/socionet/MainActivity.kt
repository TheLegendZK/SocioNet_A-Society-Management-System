package com.zhd.socionet

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.zhd.socionet.core.theme.SocioNetTheme
import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.ui.common.LocalDarkTheme
import com.zhd.socionet.ui.navigation.SocioNetNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val currentTheme by mainViewModel.currentTheme.collectAsState()

            val isDarkTheme = when (currentTheme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SocioNetTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalDarkTheme provides isDarkTheme) {
                    val view = LocalView.current
                    if (!view.isInEditMode) {
                        SideEffect {
                            val window = (view.context as Activity).window
                            // Set backgrounds too transparent to let our SoftUI background show through
                            @Suppress("DEPRECATION")
                            window.statusBarColor = Color.Transparent.toArgb()
                            @Suppress("DEPRECATION")
                            window.navigationBarColor = Color.Transparent.toArgb()

                            // Adjust Icon Colors (Dark icons for Light mode, Light icons for Dark mode)
                            val wic = WindowCompat.getInsetsController(window, view)
                            wic.isAppearanceLightStatusBars = !isDarkTheme
                            wic.isAppearanceLightNavigationBars = !isDarkTheme
                        }
                    }

                    val navController = rememberNavController()
                    SocioNetNavGraph(navController = navController)
                }
            }
        }
    }
}