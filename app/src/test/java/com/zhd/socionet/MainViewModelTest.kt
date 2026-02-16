package com.zhd.socionet

import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SettingsRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var viewModel: MainViewModel

    @Test
    fun `listens to auth and updates theme`() = runTest {
        // Given
        val userId = "user_123"
        val expectedTheme = ThemeMode.DARK

        // 1. Mock Auth Flow emitting a user ID
        every { authRepository.observeAuthState() } returns flowOf(userId)

        // 2. Mock Settings Flow emitting Dark Mode for that user
        every { settingsRepository.getThemeMode(userId) } returns flowOf(expectedTheme)

        // When
        viewModel = MainViewModel(authRepository, settingsRepository)

        // Then
        // We need to wait briefly or let the flow collect.
        // Since we use flowOf and runTest, it should happen immediately in this scope.

        verify { settingsRepository.getThemeMode(userId) }
        assertEquals(expectedTheme, viewModel.currentTheme.value)
    }

    @Test
    fun `default theme is SYSTEM`() = runTest {
        // Given
        every { authRepository.observeAuthState() } returns flowOf(null) // No user logged in

        // When
        viewModel = MainViewModel(authRepository, settingsRepository)

        // Then
        assertEquals(ThemeMode.SYSTEM, viewModel.currentTheme.value)
    }
}