package com.zhd.socionet.ui.directory

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.SocietyRepository
import com.zhd.socionet.data.repository.StaffRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SocietyDirectoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SocietyRepository = mockk()
    private val staffRepository: StaffRepository = mockk()
    private lateinit var viewModel: SocietyDirectoryViewModel

    @Test
    fun `loadDirectory sorts residents by flat ID`() = runTest {
        // Given
        val societyId = "soc_1"
        val user1 = UserProfile(fullName = "Alice", flatId = "SOC1_B_101")
        val user2 = UserProfile(fullName = "Bob", flatId = "SOC1_A_101")

        // Return unsorted list
        coEvery { repository.getSocietyResidents(societyId) } returns Result.success(listOf(user1, user2))
        coEvery { staffRepository.getStaffList(societyId) } returns emptyList()

        viewModel = SocietyDirectoryViewModel(repository, staffRepository)

        // When
        viewModel.loadDirectory(societyId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.residents.size)
        // Verify sorting: Wing A should come before Wing B
        assertEquals("SOC1_A_101", state.residents[0].flatId)
        assertEquals("SOC1_B_101", state.residents[1].flatId)
    }

    @Test
    fun `search query filters residents correctly`() = runTest {
        // Given
        val societyId = "soc_1"
        val user1 = UserProfile(fullName = "John Doe", flatId = "101")
        val user2 = UserProfile(fullName = "Jane Smith", flatId = "102")

        coEvery { repository.getSocietyResidents(societyId) } returns Result.success(listOf(user1, user2))
        coEvery { staffRepository.getStaffList(societyId) } returns emptyList()

        viewModel = SocietyDirectoryViewModel(repository, staffRepository)
        viewModel.loadDirectory(societyId)

        // When: Search by Name
        viewModel.onSearchQueryChanged("John")

        // Then
        var filtered = viewModel.uiState.value.filteredResidents
        assertEquals(1, filtered.size)
        assertEquals("John Doe", filtered[0].fullName)

        // When: Search by Flat
        viewModel.onSearchQueryChanged("102")

        // Then
        filtered = viewModel.uiState.value.filteredResidents
        assertEquals(1, filtered.size)
        assertEquals("Jane Smith", filtered[0].fullName)

        // When: Clear Search
        viewModel.onSearchQueryChanged("")

        // Then
        assertEquals(2, viewModel.uiState.value.filteredResidents.size)
    }

    @Test
    fun `loadDirectory failure sets error message`() = runTest {
        // Given
        val errorMsg = "Failed to load"
        val societyId = "soc_1"
        coEvery { repository.getSocietyResidents(any()) } returns Result.failure(Exception(errorMsg))
        coEvery { staffRepository.getStaffList(societyId) } returns emptyList()

        viewModel = SocietyDirectoryViewModel(repository, staffRepository)

        // When
        viewModel.loadDirectory(societyId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(errorMsg, state.errorMessage)
        assertEquals(0, state.residents.size)
    }
}