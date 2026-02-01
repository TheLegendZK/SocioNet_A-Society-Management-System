package com.zhd.socionet.data.model

data class SettingsPreferences(
    val notificationsEnabled: Boolean = true,
    val notifyOnVisitors: Boolean = true,
    val notifyOnNotices: Boolean = true,
    val notifyOnPayments: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val version: String = "1.0.0"
)
