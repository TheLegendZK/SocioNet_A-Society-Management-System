package com.zhd.socionet.ui.admin.staff

import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.StaffRepository
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

class StaffRegistryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: StaffRepository = mockk()
    private lateinit var viewModel: StaffRegistryViewModel

    @Before
    fun setup() {
        viewModel = StaffRegistryViewModel(repository)
    }

    @Test
    fun `loadStaff updates list in state`() = runTest {
        // Given
        val staffList = listOf(
            ServiceStaff(id = "1", name = "Ram", role = "Driver"),
            ServiceStaff(id = "2", name = "Sham", role = "Maid")
        )
        coEvery { repository.getStaffList("soc_1") } returns staffList

        // When
        viewModel.loadStaff("soc_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.staffList.size)
        assertEquals("Ram", state.staffList[0].name)
    }

    @Test
    fun `addStaff success reloads the list`() = runTest {
        // Given
        coEvery { repository.addStaff(any()) } returns Result.success(Unit)
        // Mock the reload call
        coEvery { repository.getStaffList("soc_1") } returns emptyList()

        // When
        viewModel.addStaff("New Staff", "Guard", "123", "soc_1")

        // Then
        coVerify(exactly = 1) { repository.addStaff(any()) }
        coVerify(exactly = 1) { repository.getStaffList("soc_1") }
    }

    @Test
    fun `toggleStatus updates local state immediately`() = runTest {
        // Given
        val staff = ServiceStaff(id = "1", name = "Ram", isActive = true)
        coEvery { repository.getStaffList("soc_1") } returns listOf(staff)
        viewModel.loadStaff("soc_1") // Initial load

        coEvery { repository.updateStaffStatus("1", false) } returns Result.success(Unit)

        // When
        viewModel.toggleStatus(staff)

        // Then
        val updatedStaff = viewModel.uiState.value.staffList.find { it.id == "1" }
        assertFalse("Staff should be inactive now", updatedStaff!!.isActive)

        coVerify { repository.updateStaffStatus("1", false) }
    }
}