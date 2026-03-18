package com.zhd.socionet.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

val LocalNavPadding = compositionLocalOf { PaddingValues(0.dp) }
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveAppShell(
    navController: NavController,
    navItems: List<NavItem>,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // If no nav items, just render content directly to avoid redundant Scaffold/Layout
    if (navItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftTheme.bg)
                .statusBarsPadding()
                .navigationBarsPadding() // Fixed: Ensure content/toast is above system nav bar
        ) {
            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                content(PaddingValues(0.dp))
            }
        }
        return
    }

    val navType = getNavType()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.bg)
            .statusBarsPadding()
            .consumeWindowInsets(WindowInsets.statusBars)
    ) {
        // 1. RAIL (Tablet)
        if (navType == NavType.RAIL) {
            Box(modifier = Modifier.padding(24.dp).fillMaxHeight().navigationBarsPadding()) {
                SoftCard(
                    padding = 0.dp,
                    backgroundColor = SoftTheme.surface,
                    modifier = Modifier.width(80.dp).fillMaxHeight()
                ) {
                    NavigationRail(
                        containerColor = Color.Transparent,
                        contentColor = SoftTheme.textPrimary,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(Modifier.weight(1f))
                        navItems.forEach { item ->
                            val selected = isRouteSelected(currentDestination, item)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onNavClick(navController, item, currentDestination) },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = SoftTheme.primary,
                                    indicatorColor = SoftTheme.primary.copy(alpha = 0.1f),
                                    unselectedIconColor = SoftTheme.textSecondary,
                                    unselectedTextColor = SoftTheme.textSecondary
                                )
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // 2. DRAWER (Desktop)
        if (navType == NavType.DRAWER) {
            Box(modifier = Modifier.padding(24.dp).fillMaxHeight().navigationBarsPadding()) {
                SoftCard(
                    padding = 16.dp,
                    backgroundColor = SoftTheme.surface,
                    modifier = Modifier.width(280.dp).fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "SocioNet",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.primary,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                        navItems.forEach { item ->
                            val selected = isRouteSelected(currentDestination, item)
                            NavigationDrawerItem(
                                label = { Text(item.title) },
                                selected = selected,
                                onClick = { onNavClick(navController, item, currentDestination) },
                                icon = { Icon(item.icon, null) },
                                modifier = Modifier.padding(vertical = 4.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = SoftTheme.primary.copy(alpha = 0.1f),
                                    selectedTextColor = SoftTheme.primary,
                                    selectedIconColor = SoftTheme.primary,
                                    unselectedTextColor = SoftTheme.textSecondary,
                                    unselectedIconColor = SoftTheme.textSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // MAIN CONTENT
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { 
                // Positioning SnackbarHost here ensures it is above the bottomBar
                SnackbarHost(snackbarHostState) 
            },
            bottomBar = {
                if (navType == NavType.BOTTOM_BAR) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // Fixed: Respect system navigation bar
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            color = SoftTheme.surface,
                            border = BorderStroke(
                                width = 1.dp,
                                color = SoftTheme.textSecondary.copy(alpha = 0.12f)
                            ),
                            shadowElevation = 24.dp
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0.dp),
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp)
                            ) {
                                navItems.forEach { item ->
                                    val selected = isRouteSelected(currentDestination, item)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { onNavClick(navController, item, currentDestination) },
                                        icon = { Icon(item.icon, contentDescription = item.title) },
                                        label = { Text(item.title) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SoftTheme.primary,
                                            selectedTextColor = SoftTheme.primary,
                                            indicatorColor = SoftTheme.primary.copy(alpha = 0.1f),
                                            unselectedIconColor = SoftTheme.textSecondary,
                                            unselectedTextColor = SoftTheme.textSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            CompositionLocalProvider(
                LocalNavPadding provides padding,
                LocalSnackbarHostState provides snackbarHostState
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    content(padding)
                }
            }
        }
    }
}

private fun isRouteSelected(currentDestination: NavDestination?, item: NavItem): Boolean {
    val currentRoute = currentDestination?.route ?: return false
    if (item.selectedRoutePrefix != null && currentRoute.startsWith(item.selectedRoutePrefix)) return true
    return currentDestination.hierarchy.any { it.route?.substringBefore("/") == item.route.substringBefore("/") }
}

private fun onNavClick(navController: NavController, item: NavItem, currentDestination: NavDestination?) {
    val route = item.route
    
    // Check if already on this route pattern to avoid redundant navigation flashes
    val isAlreadySelected = currentDestination?.hierarchy?.any { it.route?.startsWith(item.selectedRoutePrefix ?: route.substringBefore("/")) == true } ?: false
    
    if (isAlreadySelected && !route.contains("?")) {
        return
    }

    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}