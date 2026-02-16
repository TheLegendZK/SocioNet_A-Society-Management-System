package com.zhd.socionet.ui.directory

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.SocietyRepository
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
    private lateinit var viewModel: SocietyDirectoryViewModel

    @Test
    fun `loadDirectory sorts residents by flat ID`() = runTest {
        // Given
        val societyId = "soc_1"
        val user1 = UserProfile(fullName = "Alice", assignedFlatId = "Wing_B_101")
        val user2 = UserProfile(fullName = "Bob", assignedFlatId = "Wing_A_101")

        // Return unsorted list
        coEvery { repository.getSocietyResidents(societyId) } returns Result.success(listOf(user1, user2))

        viewModel = SocietyDirectoryViewModel(repository)

        // When
        viewModel.loadDirectory(societyId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.residents.size)
        // Verify sorting: Wing A should come before Wing B
        assertEquals("Wing_A_101", state.residents[0].assignedFlatId)
        assertEquals("Wing_B_101", state.residents[1].assignedFlatId)
    }

    @Test
    fun `search query filters residents correctly`() = runTest {
        // Given
        val societyId = "soc_1"
        val user1 = UserProfile(fullName = "John Doe", assignedFlatId = "101")
        val user2 = UserProfile(fullName = "Jane Smith", assignedFlatId = "102")

        coEvery { repository.getSocietyResidents(societyId) } returns Result.success(listOf(user1, user2))

        viewModel = SocietyDirectoryViewModel(repository)
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
        coEvery { repository.getSocietyResidents(any()) } returns Result.failure(Exception(errorMsg))

        viewModel = SocietyDirectoryViewModel(repository)

        // When
        viewModel.loadDirectory("soc_1")

        // Then
        val state = viewModel.uiState.value
        assertEquals(errorMsg, state.errorMessage)
        assertEquals(0, state.residents.size)
    }
}