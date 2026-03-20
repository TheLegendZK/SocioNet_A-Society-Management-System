package com.zhd.socionet.ui.admin.masterdata

import com.zhd.socionet.data.repository.AdminRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AdminRepository = mockk()
    private lateinit var viewModel: AdminSetupViewModel

    @Before
    fun setup() {
        // Mock init load
        coEvery { repository.getAllSocieties() } returns Result.success(emptyList())
        viewModel = AdminSetupViewModel(repository)
    }

    @Test
    fun `onSocietyDetailsChanged updates state`() = runTest {
        viewModel.onSocietyDetailsChanged("My Society", "123 Street", "REG-999")

        val state = viewModel.uiState.value
        assertEquals("My Society", state.societyName)
        assertEquals("123 Street", state.societyAddress)
        assertEquals("REG-999", state.registrationNo)
    }

    @Test
    fun `addWingConfiguration adds to list`() = runTest {
        viewModel.addWingConfiguration("A", 10, 4)

        val state = viewModel.uiState.value
        assertEquals(1, state.wingConfigurations.size)
        assertEquals("A", state.wingConfigurations[0].wingName)
    }

    @Test
    fun `submitSocietySetup success creates society and flats`() = runTest {
        // Given
        viewModel.onSocietyDetailsChanged("Soc", "Addr", "Reg")
        viewModel.addWingConfiguration("A", 2, 2) // 2 floors * 2 flats = 4 flats total

        coEvery { repository.createSociety(any()) } returns Result.success("soc_1")
        coEvery { repository.batchCreateFlats(any()) } returns Result.success(Unit)

        // When
        viewModel.submitSocietySetup()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)

        // Verify flow
        coVerify(exactly = 1) { repository.createSociety(any()) }
        // Verify the logic inside VM generated flats and passed them to repository
        coVerify(exactly = 1) {
            repository.batchCreateFlats(match { flats ->
                flats.size == 4 && flats[0].societyId == "soc_1"
            })
        }
    }

    @Test
    fun `submitSocietySetup failure in creation sets error`() = runTest {
        // Given
        viewModel.onSocietyDetailsChanged("Soc", "Addr", "Reg")
        coEvery { repository.createSociety(any()) } returns Result.failure(Exception("DB Error"))

        // When
        viewModel.submitSocietySetup()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("DB Error", state.errorMessage)

        // Should NOT try to create flats if society creation failed
        coVerify(exactly = 0) { repository.batchCreateFlats(any()) }
    }
}
