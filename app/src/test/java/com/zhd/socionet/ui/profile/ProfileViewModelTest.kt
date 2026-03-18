package com.zhd.socionet.ui.profile

import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.ProfileRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ProfileRepository = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        viewModel = ProfileViewModel(repository, firestore)
    }

    @Test
    fun `loadProfile success updates state`() = runTest {
        // Given
        val userId = "user_123"
        val mockProfile = UserProfile(id = userId, fullName = "Test User")
        coEvery { repository.getUserProfile(userId) } returns Result.success(mockProfile)

        // When
        viewModel.loadProfile(userId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(mockProfile, state.userProfile)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadProfile failure updates error message`() = runTest {
        // Given
        val userId = "user_123"
        val errorMsg = "Network error"
        coEvery { repository.getUserProfile(userId) } returns Result.failure(Exception(errorMsg))

        // When
        viewModel.loadProfile(userId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.userProfile)
        assertEquals(errorMsg, state.errorMessage)
    }

    @Test
    fun `updateProfile success updates local profile and success message`() = runTest {
        // Given: We need an existing profile in state to update it
        val userId = "user_123"
        val initialProfile = UserProfile(id = userId, fullName = "Old Name", phoneNumber = "111")

        // 1. Mock loading the initial profile
        coEvery { repository.getUserProfile(userId) } returns Result.success(initialProfile)
        viewModel.loadProfile(userId)

        // 2. Mock the update call
        coEvery { repository.updateUserProfile(any()) } returns Result.success(Unit)

        // When
        viewModel.updateProfile("New Name", "222")

        // Then
        val state = viewModel.uiState.value
        assertEquals("New Name", state.userProfile?.fullName)
        assertEquals("222", state.userProfile?.phoneNumber)
        assertEquals("Profile Updated", state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `updateProfile failure sets error message`() = runTest {
        // Given
        val userId = "user_123"
        val initialProfile = UserProfile(id = userId, fullName = "Old Name")
        coEvery { repository.getUserProfile(userId) } returns Result.success(initialProfile)
        viewModel.loadProfile(userId)

        val errorMsg = "Update failed"
        coEvery { repository.updateUserProfile(any()) } returns Result.failure(Exception(errorMsg))

        // When
        viewModel.updateProfile("New Name", "222")

        // Then
        val state = viewModel.uiState.value
        assertEquals(errorMsg, state.errorMessage)
        // Ensure success message is null
        assertNull(state.successMessage)
    }

    @Test
    fun `changePassword success sets success message`() = runTest {
        // Given
        coEvery { repository.updatePassword(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.changePassword("oldPass", "newPass")

        // Then
        val state = viewModel.uiState.value
        assertEquals("Password Changed Successfully", state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `changePassword failure sets error message`() = runTest {
        // Given
        val errorMsg = "Weak password"
        coEvery { repository.updatePassword(any(), any()) } returns Result.failure(Exception(errorMsg))

        // When
        viewModel.changePassword("oldPass", "newPass")

        // Then
        val state = viewModel.uiState.value
        assertEquals(errorMsg, state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun `logout calls repository and callback`() = runTest {
        // Given
        coEvery { repository.logout() } returns Unit
        var logoutCallbackTriggered = false

        // When
        viewModel.logout {
            logoutCallbackTriggered = true
        }

        // Then
        coVerify(exactly = 1) { repository.logout() }
        assertTrue(logoutCallbackTriggered)
    }
}
