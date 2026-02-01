package com.zhd.socionet.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

enum class NavType { BOTTOM_BAR, RAIL, DRAWER }

@Composable
fun getNavType(): NavType {
    val config = LocalConfiguration.current
    val width = config.screenWidthDp.dp
    return when {
        width < 600.dp -> NavType.BOTTOM_BAR
        width < 840.dp -> NavType.RAIL
        else -> NavType.DRAWER
    }
}