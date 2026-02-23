package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSettingsRepository : SettingsRepository {
    override fun getThemeMode(userId: String): Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    override suspend fun getSettings(userId: String): Result<SettingsPreferences> = Result.success(SettingsPreferences())
    override suspend fun updateSettings(userId: String, settings: SettingsPreferences): Result<Unit> = Result.success(Unit)
    override fun setNotificationTopic(topic: String, subscribe: Boolean) {}
}
