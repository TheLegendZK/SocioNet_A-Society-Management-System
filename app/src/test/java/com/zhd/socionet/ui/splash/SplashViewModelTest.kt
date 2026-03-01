package com.zhd.socionet.ui.splash

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.ProfileRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private lateinit var viewModel: SplashViewModel

    @Test
    fun `checkAuthStatus returns null if not logged in`() = runTest {
        // Given
        every { authRepository.getCurrentUserId() } returns null
        viewModel = SplashViewModel(authRepository, profileRepository)

        // When
        var result: String? = "default"
        viewModel.checkAuthStatus { result = it }

        // FIX: Wait for delay(1500) to complete
        advanceUntilIdle()

        // Then
        assertNull(result)
    }

    @Test
    fun `checkAuthStatus returns Admin Dashboard for Super Admin`() = runTest {
        // Given
        val adminId = "admin_1"
        val profile = UserProfile(id = adminId, roles = listOf(UserRole.SUPER_ADMIN))

        every { authRepository.getCurrentUserId() } returns adminId
        coEvery { profileRepository.getUserProfile(adminId) } returns Result.success(profile)

        viewModel = SplashViewModel(authRepository, profileRepository)

        // When
        var result: String? = null
        viewModel.checkAuthStatus { result = it }

        advanceUntilIdle() // Wait for delay

        // Then
        assertEquals("admin_dashboard", result)
    }

    @Test
    fun `checkAuthStatus returns Resident Home for Resident`() = runTest {
        // Given
        val userId = "user_1"
        val flatId = "A_101"
        val profile = UserProfile(id = userId, roles = listOf(UserRole.OWNER), flatId = flatId)

        every { authRepository.getCurrentUserId() } returns userId
        coEvery { profileRepository.getUserProfile(userId) } returns Result.success(profile)

        viewModel = SplashViewModel(authRepository, profileRepository)

        // When
        var result: String? = null
        viewModel.checkAuthStatus { result = it }

        advanceUntilIdle() // Wait for delay

        // Then
        assertEquals("resident_home/$flatId/$userId", result)
    }
}