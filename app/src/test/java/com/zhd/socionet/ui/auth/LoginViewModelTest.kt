package com.zhd.socionet.ui.auth

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    @Test
    fun `login success updates state and triggers callback`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val mockProfile = UserProfile(id = "123", fullName = "Test User")

        coEvery { repository.loginUser(email, password) } returns Result.success(mockProfile)

        viewModel = LoginViewModel(repository)
        var callbackTriggered = false

        // When
        viewModel.login(email, password) { profile ->
            callbackTriggered = true
            assertEquals("123", profile.id)
        }

        // Then
        val state = viewModel.uiState.value
        assertFalse("Loading should stop after success", state.isLoading)
        assertNull("Error message should be null on success", state.errorMessage)
        assertEquals("Callback should be triggered", true, callbackTriggered)
    }

    @Test
    fun `login failure updates error message`() = runTest {
        // Given
        val email = "wrong@example.com"
        val password = "wrong"
        val errorMessage = "Invalid credentials"

        coEvery { repository.loginUser(email, password) } returns Result.failure(Exception(errorMessage))

        viewModel = LoginViewModel(repository)
        var callbackTriggered = false

        // When
        viewModel.login(email, password) {
            callbackTriggered = true
        }

        // Then
        val state = viewModel.uiState.value
        assertFalse("Loading should stop after failure", state.isLoading)
        assertEquals("Error message should match", errorMessage, state.errorMessage)
        assertEquals("Callback should NOT be triggered", false, callbackTriggered)
    }
}