package com.zhd.socionet.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveAppShell(
    navController: NavController,
    navItems: List<NavItem>,
    content: @Composable (PaddingValues) -> Unit
) {
    val navType = getNavType()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Use Dynamic Background
    Row(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
        // 1. RAIL (Tablet)
        if (navType == NavType.RAIL) {
            NavigationRail(
                containerColor = SoftTheme.surface,
                contentColor = SoftTheme.textPrimary,
                header = { Box(modifier = Modifier.size(48.dp).background(SoftTheme.primary, CircleShape)) }
            ) {
                Spacer(Modifier.weight(1f))
                navItems.forEach { item ->
                    val selected = isRouteSelected(currentDestination, item)
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavClick(navController, item.route) },
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

        // 2. DRAWER (Desktop)
        if (navType == NavType.DRAWER) {
            Column(
                modifier = Modifier.width(250.dp).fillMaxHeight().background(SoftTheme.surface).padding(16.dp)
            ) {
                Text("SocioNet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SoftTheme.primary, modifier = Modifier.padding(vertical = 24.dp))
                navItems.forEach { item ->
                    val selected = isRouteSelected(currentDestination, item)
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = { onNavClick(navController, item.route) },
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

        // MAIN CONTENT
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (navType == NavType.BOTTOM_BAR) {
                    NavigationBar(
                        containerColor = SoftTheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = isRouteSelected(currentDestination, item)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onNavClick(navController, item.route) },
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
        ) { padding ->
            Box(modifier = Modifier.padding(padding).consumeWindowInsets(padding)) { content(padding) }
        }
    }
}

private fun isRouteSelected(currentDestination: NavDestination?, item: NavItem): Boolean {
    val currentRoute = currentDestination?.route ?: return false
    if (item.selectedRoutePrefix != null) {
        if (currentRoute.startsWith(item.selectedRoutePrefix)) return true
    }
    val baseRoute = item.route.substringBefore("/")
    return currentRoute.contains(baseRoute)
}

private fun onNavClick(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}