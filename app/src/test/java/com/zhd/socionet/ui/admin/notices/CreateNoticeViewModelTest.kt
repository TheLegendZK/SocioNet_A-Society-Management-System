package com.zhd.socionet.ui.admin.notices

import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.NoticeRepository
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

class CreateNoticeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noticeRepository: NoticeRepository = mockk()
    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: CreateNoticeViewModel

    @Before
    fun setup() {
        viewModel = CreateNoticeViewModel(noticeRepository, authRepository)
    }

    @Test
    fun `loadWings fetches wings from auth repo`() = runTest {
        // Given
        val wings = listOf("A", "B", "C")
        coEvery { authRepository.fetchWings("soc_1") } returns wings

        // When
        viewModel.loadWings("soc_1")

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.availableWings.size)
        assertEquals("B", state.availableWings[1])
    }

    @Test
    fun `postNotice success updates state`() = runTest {
        // Given
        viewModel.onFieldChange("Title", "Content")
        viewModel.onPrioritySelected("HIGH")

        coEvery { noticeRepository.createNotice(any()) } returns Result.success(Unit)

        // When
        viewModel.postNotice("admin_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)

        coVerify {
            noticeRepository.createNotice(match {
                it.title == "Title" && it.priority == "HIGH" && it.postedByAdminId == "admin_1"
            })
        }
    }

    @Test
    fun `postNotice failure sets error message`() = runTest {
        // Given
        viewModel.onFieldChange("Title", "Content")
        coEvery { noticeRepository.createNotice(any()) } returns Result.failure(Exception("Network error"))

        // When
        viewModel.postNotice("admin_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Failed to post notice", state.errorMessage)
    }
}