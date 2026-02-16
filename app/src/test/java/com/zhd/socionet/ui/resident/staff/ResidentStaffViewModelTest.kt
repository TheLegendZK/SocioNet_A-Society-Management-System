package com.zhd.socionet.ui.resident.staff

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

class ResidentStaffViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: StaffRepository = mockk()
    private lateinit var viewModel: ResidentStaffViewModel

    @Before
    fun setup() {
        viewModel = ResidentStaffViewModel(repository)
    }

    @Test
    fun `loadStaff updates state successfully`() = runTest {
        // Given
        val staffList = listOf(ServiceStaff(id = "s1", name = "Raju"))
        coEvery { repository.getStaffList("soc_1") } returns staffList

        // When
        viewModel.loadStaff("soc_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.allStaff.size)
        assertEquals("Raju", state.allStaff[0].name)
    }

    @Test
    fun `toggleLink ADDS staff if not currently linked (Optimistic Update)`() = runTest {
        // Given: Staff list loaded, user is NOT in employer list
        val flatId = "flat_101"
        val staff = ServiceStaff(id = "s1", name = "Raju", employerFlatIds = emptyList())

        coEvery { repository.getStaffList(any()) } returns listOf(staff)
        viewModel.loadStaff("soc_1") // Load initial data

        coEvery { repository.linkStaffToFlat("s1", flatId) } returns Result.success(Unit)

        // When
        viewModel.toggleLink(staff, flatId)

        // Then: Verify Repository Call
        coVerify { repository.linkStaffToFlat("s1", flatId) }

        // Then: Verify State Updated Optimistically (Locally)
        val updatedStaff = viewModel.uiState.value.allStaff.find { it.id == "s1" }
        assertTrue("Flat ID should be added to employers", updatedStaff!!.employerFlatIds.contains(flatId))
    }

    @Test
    fun `toggleLink REMOVES staff if currently linked`() = runTest {
        // Given: User IS in employer list
        val flatId = "flat_101"
        val staff = ServiceStaff(id = "s1", name = "Raju", employerFlatIds = listOf(flatId))

        coEvery { repository.getStaffList(any()) } returns listOf(staff)
        viewModel.loadStaff("soc_1")

        coEvery { repository.unlinkStaffFromFlat("s1", flatId) } returns Result.success(Unit)

        // When
        viewModel.toggleLink(staff, flatId)

        // Then
        coVerify { repository.unlinkStaffFromFlat("s1", flatId) }

        // Then: Verify State Updated
        val updatedStaff = viewModel.uiState.value.allStaff.find { it.id == "s1" }
        assertFalse("Flat ID should be removed from employers", updatedStaff!!.employerFlatIds.contains(flatId))
    }
}