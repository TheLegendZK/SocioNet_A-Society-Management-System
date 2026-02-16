package com.zhd.socionet.ui.resident.notices

import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.NoticeRepository
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

class NoticeBoardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: NoticeRepository = mockk()
    private lateinit var viewModel: NoticeBoardViewModel

    @Before
    fun setup() {
        viewModel = NoticeBoardViewModel(repository)
    }

    @Test
    fun `loadNotices for Owner passes isOwner=true`() = runTest {
        // Given
        val wing = "A"
        val roles = listOf(UserRole.OWNER, UserRole.OWNER) // User is an Owner
        val mockNotices = listOf(Notice(id = "1", title = "AGM Meeting"))

        coEvery { repository.getNoticesForUser(wing, isOwner = true) } returns mockNotices

        // When
        viewModel.loadNotices(wing, roles)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.notices.size)
        assertEquals("AGM Meeting", state.notices[0].title)

        // Verify correct flag was passed
        coVerify { repository.getNoticesForUser(wing, isOwner = true) }
    }

    @Test
    fun `loadNotices for Tenant passes isOwner=false`() = runTest {
        // Given
        val wing = "B"
        val roles = listOf(UserRole.TENANT) // User is NOT an Owner

        coEvery { repository.getNoticesForUser(wing, isOwner = false) } returns emptyList()

        // When
        viewModel.loadNotices(wing, roles)

        // Then
        coVerify { repository.getNoticesForUser(wing, isOwner = false) }
    }
}