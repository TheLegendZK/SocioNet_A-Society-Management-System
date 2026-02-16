package com.zhd.socionet.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.zhd.socionet.core.theme.*

val LocalDarkTheme = compositionLocalOf { false }

object SoftTheme {
    val bg: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val surface: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val primary: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val textPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val textSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val shadowLight: Color
        @Composable get() = if (LocalDarkTheme.current) Color.Black.copy(alpha = 0f) else Color.White

    val shadowDark: Color
        @Composable get() = if (LocalDarkTheme.current) Color.Black.copy(alpha = 0.5f) else Color(0xFFA3B1C6).copy(alpha = 0.3f)

    // Semantic Colors
    val info: Color = SoftInfo
    val success: Color = SoftSuccess
    val warning: Color = SoftWarning
    val purple: Color = SoftPurple
}

val SoftAccent = Color(0xFFFF6B6B)

val GradientPrimary
    @Composable get() = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )
