package com.zhd.socionet.ui.admin.complaints

import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.repository.ComplaintRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class AdminComplaintViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ComplaintRepository = mockk()
    private lateinit var viewModel: AdminComplaintViewModel

    @Test
    fun `init loads all complaints`() = runTest {
        // Given
        val complaints = listOf(
            Complaint(id = "1", title = "Leak", status = "OPEN"),
            Complaint(id = "2", title = "Noise", status = "OPEN")
        )
        coEvery { repository.getAllComplaints() } returns complaints

        // When (ViewModel init block calls loadAllComplaints)
        viewModel = AdminComplaintViewModel(repository)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.complaints.size)
        assertEquals("Leak", state.complaints[0].title)
    }

    @Test
    fun `updateStatus success updates local list state`() = runTest {
        // Given
        val complaint = Complaint(id = "1", title = "Leak", status = "OPEN")
        coEvery { repository.getAllComplaints() } returns listOf(complaint)

        viewModel = AdminComplaintViewModel(repository) // loads initial list

        // Mock update
        coEvery { repository.updateStatus("1", "RESOLVED") } returns Result.success(Unit)

        // When
        viewModel.updateStatus(complaint, "RESOLVED")

        // Then
        val state = viewModel.uiState.value
        val updatedComplaint = state.complaints.find { it.id == "1" }

        assertEquals("RESOLVED", updatedComplaint?.status)
        coVerify { repository.updateStatus("1", "RESOLVED") }
    }

    @Test
    fun `updateStatus failure does not update local list`() = runTest {
        // Given
        val complaint = Complaint(id = "1", title = "Leak", status = "OPEN")
        coEvery { repository.getAllComplaints() } returns listOf(complaint)

        viewModel = AdminComplaintViewModel(repository)

        // Mock failure
        coEvery { repository.updateStatus("1", "RESOLVED") } returns Result.failure(Exception("Error"))

        // When
        viewModel.updateStatus(complaint, "RESOLVED")

        // Then
        val state = viewModel.uiState.value
        val item = state.complaints.find { it.id == "1" }

        // Status should remain OPEN
        assertEquals("OPEN", item?.status)
    }
}