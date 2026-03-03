package com.zhd.socionet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.zhd.socionet.ui.navigation.Routes
import com.zhd.socionet.ui.navigation.SocioNetNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle deep link if app is launched from notification
        mainViewModel.handleDeepLink(intent)

        setContent {
            val currentTheme by mainViewModel.currentTheme.collectAsState()
            val deepLinkRoute by mainViewModel.deepLinkRoute.collectAsState()
            
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
                            @Suppress("DEPRECATION")
                            window.statusBarColor = Color.Transparent.toArgb()
                            @Suppress("DEPRECATION")
                            window.navigationBarColor = Color.Transparent.toArgb()
                            val wic = WindowCompat.getInsetsController(window, view)
                            wic.isAppearanceLightStatusBars = !isDarkTheme
                            wic.isAppearanceLightNavigationBars = !isDarkTheme
                        }
                    }

                    val navController = rememberNavController()
                    
                    // Handle deep link navigation
                    LaunchedEffect(deepLinkRoute) {
                        deepLinkRoute?.let { route ->
                            navController.navigate(route) {
                                // If we are at Splash, clear it
                                popUpTo(Routes.SPLASH) { inclusive = true }
                                // If we are already elsewhere (background click), don't clear everything, just navigate
                            }
                            mainViewModel.onDeepLinkHandled()
                        }
                    }

                    SocioNetNavGraph(
                        navController = navController,
                        isDeepLinkPending = deepLinkRoute != null // Pass the state
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.handleDeepLink(intent)
    }
}
