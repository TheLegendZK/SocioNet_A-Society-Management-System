package com.zhd.socionet.ui.resident.complaints

import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.data.repository.ComplaintRepository
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

class ComplaintViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ComplaintRepository = mockk()
    private lateinit var viewModel: ComplaintViewModel

    @Before
    fun setup() {
        viewModel = ComplaintViewModel(repository)
    }

    @Test
    fun `loadComplaints updates state`() = runTest {
        // Given
        val complaints = listOf(Complaint(id = "1", title = "Broken Tap"))
        coEvery { repository.getComplaintsForFlat("flat_1") } returns complaints

        // When
        viewModel.loadComplaints("flat_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.complaints.size)
        assertEquals("Broken Tap", state.complaints[0].title)
    }

    @Test
    fun `submitComplaint success reloads list`() = runTest {
        // Given
        val flatId = "flat_1"
        coEvery { repository.fileComplaint(any()) } returns Result.success(Unit)
        // Mock the reload that happens after success
        coEvery { repository.getComplaintsForFlat(flatId) } returns listOf(Complaint(id = "new"))

        // When
        viewModel.submitComplaint("Title", "Desc", ComplaintCategory.PLUMBING, flatId, "user_1")

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertEquals(1, state.complaints.size)

        coVerify {
            repository.fileComplaint(match {
                it.title == "Title" && it.category == ComplaintCategory.PLUMBING && it.status == "OPEN"
            })
        }
    }

    @Test
    fun `submitComplaint failure sets error message`() = runTest {
        // Given
        coEvery { repository.fileComplaint(any()) } returns Result.failure(Exception("DB Error"))

        // When
        viewModel.submitComplaint("Title", "Desc", ComplaintCategory.PLUMBING, "flat_1", "user_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Failed to submit complaint", state.errorMessage)
    }
}