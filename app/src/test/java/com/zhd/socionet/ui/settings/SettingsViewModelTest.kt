package com.zhd.socionet.ui.settings

import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SettingsRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    private val userId = "user_123"

    @Before
    fun setup() {
        every { authRepository.getCurrentUserId() } returns userId
        coEvery { settingsRepository.getSettings(userId) } returns Result.success(SettingsPreferences())
        coEvery { settingsRepository.updateSettings(any(), any()) } returns Result.success(Unit)

        viewModel = SettingsViewModel(authRepository, settingsRepository)
    }

    @Test
    fun `loadSettings fetches prefs on init`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { settingsRepository.getSettings(userId) }
        verify { settingsRepository.setNotificationTopic("all_alerts", true) }
    }

    @Test
    fun `loadSettings failure updates error message`() = runTest {
        // Reset and mock failure
        coEvery { settingsRepository.getSettings(userId) } returns Result.failure(Exception("Load failed"))
        
        // Re-init to trigger loadSettings
        viewModel = SettingsViewModel(authRepository, settingsRepository)

        val state = viewModel.uiState.value
        assertEquals("Failed to load settings", state.errorMessage)
    }

    @Test
    fun `toggleNotifications updates repo and topics`() = runTest {
        viewModel.toggleNotifications(false)

        val state = viewModel.uiState.value
        assertFalse(state.prefs.notificationsEnabled)

        coVerify { settingsRepository.updateSettings(userId, match { !it.notificationsEnabled }) }
        verify { settingsRepository.setNotificationTopic("all_alerts", false) }
        verify { settingsRepository.setNotificationTopic("notices", false) }
        verify { settingsRepository.setNotificationTopic("visitors", false) }
    }

    @Test
    fun `toggleVisitorAlerts updates specific topic`() = runTest {
        viewModel.toggleVisitorAlerts(false)

        val state = viewModel.uiState.value
        assertFalse(state.prefs.notifyOnVisitors)
        verify { settingsRepository.setNotificationTopic("visitors", false) }
    }

    @Test
    fun `toggleNoticeAlerts updates specific topic`() = runTest {
        viewModel.toggleNoticeAlerts(false)

        val state = viewModel.uiState.value
        assertFalse(state.prefs.notifyOnNotices)
        verify { settingsRepository.setNotificationTopic("notices", false) }
    }

    @Test
    fun `setTheme updates state and repository`() = runTest {
        viewModel.setTheme(ThemeMode.DARK)

        val state = viewModel.uiState.value
        assertEquals(ThemeMode.DARK, state.prefs.themeMode)
        coVerify { settingsRepository.updateSettings(userId, match { it.themeMode == ThemeMode.DARK }) }
    }

    @Test
    fun `saveSettings failure updates error message`() = runTest {
        coEvery { settingsRepository.updateSettings(any(), any()) } returns Result.failure(Exception("Save failed"))

        viewModel.setTheme(ThemeMode.LIGHT)

        val state = viewModel.uiState.value
        assertEquals("Failed to save changes", state.errorMessage)
    }
}
