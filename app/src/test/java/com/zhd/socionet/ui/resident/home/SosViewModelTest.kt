package com.zhd.socionet.ui.resident.home

import com.zhd.socionet.data.repository.SosRepositoryImpl
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SosRepositoryImpl = mockk()
    private lateinit var viewModel: SosViewModel

    @Test
    fun `triggerEmergency calls repository`() = runTest {
        // Given
        coEvery { repository.triggerSos(any(), any(), any()) } returns Result.success(Unit)
        viewModel = SosViewModel(repository)

        // When
        viewModel.triggerEmergency("flat_101", "user_123")

        // Then
        coVerify {
            repository.triggerSos("user_123", "flat_101", "General Emergency")
        }
    }
}