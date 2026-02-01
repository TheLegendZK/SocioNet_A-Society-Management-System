package com.zhd.socionet.ui.common

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val selectedRoutePrefix: String? = null,
)
