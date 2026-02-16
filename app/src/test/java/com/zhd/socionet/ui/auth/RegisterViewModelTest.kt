package com.zhd.socionet.ui.auth

import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.AuthRepository
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

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AuthRepository = mockk()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        // Default mocks for init block
        coEvery { repository.fetchSocieties() } returns listOf("soc1" to "Society A", "soc2" to "Society B")
        viewModel = RegisterViewModel(repository)
    }

    @Test
    fun `init loads societies correctly`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(2, state.societies.size)
        assertEquals("Society A", state.societies[0].second)
    }

    @Test
    fun `selecting society loads wings`() = runTest {
        // Given
        val societyId = "soc1"
        val wings = listOf("A", "B")
        coEvery { repository.fetchWings(societyId) } returns wings

        // When
        viewModel.onSocietySelected(societyId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(societyId, state.selectedSocietyId)
        assertEquals(wings, state.wings)
        assertEquals("", state.selectedWing) // Should reset wing
    }

    @Test
    fun `register fails if no flat selected`() = runTest {
        // Given
        viewModel.onRoleSelected(UserRole.TENANT)
        // No flat selected in state

        // When
        viewModel.registerUser("email", "pass", "name", "phone")

        // Then
        val state = viewModel.uiState.value
        assertEquals("Please select a flat first.", state.errorMessage)
        coVerify(exactly = 0) { repository.registerUser(any(), any(), any()) }
    }

    @Test
    fun `register owner fails if flat already occupied`() = runTest {
        // Given
        val flatId = "flat_101"
        viewModel.onFlatSelected(flatId)
        viewModel.onRoleSelected(UserRole.OWNER)

        // Mock that flat has an owner
        coEvery { repository.checkFlatOccupancy(flatId) } returns "existing_owner_id"

        // When
        viewModel.registerUser("email", "pass", "name", "phone")

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage!!.contains("Security Alert"))
        coVerify(exactly = 0) { repository.registerUser(any(), any(), any()) }
    }

    @Test
    fun `register success updates state`() = runTest {
        // Given
        val flatId = "flat_101"
        viewModel.onFlatSelected(flatId)
        viewModel.onRoleSelected(UserRole.TENANT) // No occupancy check needed for tenant logic in current VM (or assumed empty)

        coEvery { repository.registerUser(any(), any(), any()) } returns Result.success("user_123")

        // When
        viewModel.registerUser("email", "pass", "name", "phone")

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { repository.registerUser(any(), any(), any()) }
    }
}