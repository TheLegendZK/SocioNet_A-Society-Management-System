package com.zhd.socionet.ui.settings

import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SettingsRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true) // Relaxed for Unit returns
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        every { authRepository.getCurrentUserId() } returns "user_123"
        // Default settings behavior
        coEvery { settingsRepository.getSettings("user_123") } returns Result.success(SettingsPreferences())
        coEvery { settingsRepository.updateSettings(any(), any()) } returns Result.success(Unit)

        viewModel = SettingsViewModel(authRepository, settingsRepository)
    }

    @Test
    fun `loadSettings fetches prefs on init`() = runTest {
        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { settingsRepository.getSettings("user_123") }
        // Verify default subscription sync
        verify { settingsRepository.setNotificationTopic("all_alerts", true) }
    }

    @Test
    fun `toggleNotifications updates repo and topics`() = runTest {
        // When
        viewModel.toggleNotifications(false)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.prefs.notificationsEnabled)

        coVerify { settingsRepository.updateSettings("user_123", match { !it.notificationsEnabled }) }
        // Verify unsubscribing from everything
        verify { settingsRepository.setNotificationTopic("all_alerts", false) }
    }

    @Test
    fun `toggleVisitorAlerts updates specific topic`() = runTest {
        // Given notifications are ON by default

        // When
        viewModel.toggleVisitorAlerts(false)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.prefs.notifyOnVisitors)
        verify { settingsRepository.setNotificationTopic("visitors", false) }
    }
}