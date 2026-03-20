package com.zhd.socionet.ui.resident.home

import com.zhd.socionet.data.model.EmergencyAlert
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SosRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SosRepository = mockk()
    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: SosViewModel

    @Before
    fun setup() {
        // Default stub for authRepository
        coEvery { authRepository.getUserProfile(any()) } returns null
        viewModel = SosViewModel(repository, authRepository)
    }

    @Test
    fun `triggerEmergency calls repository`() = runTest {
        // Given
        coEvery { repository.triggerSos(any()) } returns Result.success(Unit)

        // When
        viewModel.triggerEmergency("flat_101", "user_123")

        // Then
        coVerify {
            repository.triggerSos(match {
                it.raisedByUserId == "user_123" && it.flatId == "flat_101" && it.subType == "General Emergency"
            })
        }
    }
}
