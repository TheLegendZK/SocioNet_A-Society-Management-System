package com.zhd.socionet.ui.resident.gatepass

import com.zhd.socionet.core.utils.OtpGenerator
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.StaffRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GatePassViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: GatePassRepository = mockk()
    private val staffRepository: StaffRepository = mockk()
    private val otpGenerator: OtpGenerator = mockk()
    private lateinit var viewModel: GatePassViewModel

    @Before
    fun setup() {
        viewModel = GatePassViewModel(repository, staffRepository, otpGenerator)
    }

    @Test
    fun `loadPendingRequests collects flow`() = runTest {
        // Given
        val entry = GateEntry(id = "1", visitorName = "John")
        // Mock the Flow return
        every { repository.getPendingRequestsFlow("flat_1") } returns flowOf(listOf(entry))

        // When
        viewModel.loadPendingRequests("flat_1")

        // Then
        // We don't need to wait or advance time because flowOf emits immediately
        val state = viewModel.uiState.value
        assertEquals(1, state.pendingRequests.size)
        assertEquals("John", state.pendingRequests[0].visitorName)
    }

    @Test
    fun `generatePass generates OTP and calls repository`() = runTest {
        // Given
        viewModel.onVisitorNameChanged("Zomato")
        viewModel.onVisitorTypeChanged(VisitorType.DELIVERY)

        every { otpGenerator.generateOtp() } returns "123456"
        coEvery { repository.createGatePass(any()) } returns Result.success("pass_id_1")

        // When
        viewModel.generatePass("flat_1", "user_1")

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals("123456", state.generatedOtp)
        assertEquals("pass_id_1", state.generatedPassId)

        coVerify {
            repository.createGatePass(match {
                it.visitorName == "Zomato" && it.entryCode == "123456" && it.status == EntryStatus.APPROVED
            })
        }
    }

    @Test
    fun `respondToRequest Approve calls repository with APPROVED`() = runTest {
        val entry = GateEntry(id = "1")
        coEvery { repository.respondToRequest("1", EntryStatus.APPROVED) } returns Result.success(Unit)

        viewModel.respondToRequest(entry, true) // accept = true

        coVerify { repository.respondToRequest("1", EntryStatus.APPROVED) }
    }

    @Test
    fun `loadLinkedStaffAndLogs filters staff correctly`() = runTest {
        // Given
        val myFlat = "flat_101"
        val staff1 = ServiceStaff(id = "s1", name = "Ram", employerFlatIds = listOf(myFlat)) // Linked
        val staff2 = ServiceStaff(id = "s2", name = "Sham", employerFlatIds = listOf("flat_202")) // Not linked

        coEvery { staffRepository.getStaffList("soc_1") } returns listOf(staff1, staff2)

        // Mock logs flow for the filtered staff ID (s1)
        val logs = listOf(GateEntry(id = "log1", visitorName = "Ram"))
        every { repository.getStaffLogsFlow(listOf("s1")) } returns flowOf(logs)

        // When
        viewModel.loadLinkedStaffAndLogs("soc_1", myFlat)

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.linkedStaff.size)
        assertEquals("Ram", state.linkedStaff[0].name)
        assertEquals(1, state.staffLogs.size)
    }
}