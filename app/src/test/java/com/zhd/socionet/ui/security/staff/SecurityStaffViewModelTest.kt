package com.zhd.socionet.ui.security.staff

import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.StaffRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SecurityStaffViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val staffRepo: StaffRepository = mockk()
    private val gateRepo: GatePassRepository = mockk()
    private lateinit var viewModel: SecurityStaffViewModel

    @Before
    fun setup() {
        viewModel = SecurityStaffViewModel(staffRepo, gateRepo)
    }

    @Test
    fun `loadStaff loads list correctly`() = runTest {
        // Given
        val staffList = listOf(ServiceStaff(id = "1", name = "Raju"))
        coEvery { staffRepo.getStaffList("soc_1") } returns staffList

        // When
        viewModel.loadStaff("soc_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.staffList.size)
        assertEquals("Raju", state.staffList[0].name)
    }

    @Test
    fun `markAttendance calls gate repo`() = runTest {
        // Given
        val staff = ServiceStaff(id = "s1", name = "Raju")
        coEvery { gateRepo.logStaffEntry("s1", "Raju", "IN") } returns Result.success(Unit)

        // When
        viewModel.markAttendance(staff, "IN")

        // Then
        coVerify { gateRepo.logStaffEntry("s1", "Raju", "IN") }
    }
}