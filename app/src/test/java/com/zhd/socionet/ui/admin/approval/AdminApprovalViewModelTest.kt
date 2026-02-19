package com.zhd.socionet.ui.admin.approval

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.AdminUserRepository
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

class AdminApprovalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AdminUserRepository = mockk()
    private lateinit var viewModel: AdminApprovalViewModel

    @Before
    fun setup() {
        viewModel = AdminApprovalViewModel(repository)
    }

    @Test
    fun `loadPendingRequests updates state with users`() = runTest {
        // Given
        val societyId = "soc_1"
        val users = listOf(
            UserProfile(id = "1", fullName = "User A"),
            UserProfile(id = "2", fullName = "User B")
        )
        coEvery { repository.getPendingUsers(societyId) } returns users

        // When
        viewModel.loadPendingRequests(societyId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.pendingUsers.size)
        assertEquals("User A", state.pendingUsers[0].fullName)
    }

    @Test
    fun `approveUser success removes user from list and sets message`() = runTest {
        // Given: State must have users first
        val userToApprove = UserProfile(id = "1", fullName = "User A")
        val otherUser = UserProfile(id = "2", fullName = "User B")

        // Mock loading
        coEvery { repository.getPendingUsers(any()) } returns listOf(userToApprove, otherUser)
        viewModel.loadPendingRequests("soc_1")

        // Mock approval
        coEvery { repository.approveUser("1") } returns Result.success(Unit)

        // When
        viewModel.approveUser(userToApprove)

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.pendingUsers.size) // Should be reduced by 1
        assertEquals("User B", state.pendingUsers[0].fullName) // Only User B remains
        assertTrue(state.message!!.contains("Approved"))

        coVerify { repository.approveUser("1") }
    }

    @Test
    fun `rejectUser success removes user from list`() = runTest {
        // Given
        val userToReject = UserProfile(id = "1", fullName = "User A")

        coEvery { repository.getPendingUsers(any()) } returns listOf(userToReject)
        viewModel.loadPendingRequests("soc_1")

        coEvery { repository.rejectUser("1") } returns Result.success(Unit)

        // When
        viewModel.rejectUser(userToReject)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.pendingUsers.isEmpty())
        assertTrue(state.message!!.contains("Rejected"))
    }

    @Test
    fun `approveUser failure sets error message`() = runTest {
        // Given
        val user = UserProfile(id = "1", fullName = "User A")
        val errorMsg = "Network Error"

        coEvery { repository.approveUser("1") } returns Result.failure(Exception(errorMsg))

        // When
        viewModel.approveUser(user)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.message!!.contains("Failed to approve"))
        assertTrue(state.message!!.contains(errorMsg))
    }
}