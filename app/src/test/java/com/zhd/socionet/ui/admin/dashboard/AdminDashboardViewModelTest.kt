package com.zhd.socionet.ui.admin.dashboard

import com.zhd.socionet.data.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AdminDashboardViewModelTest {

    private val repository: AuthRepository = mockk()
    private lateinit var viewModel: AdminDashboardViewModel

    @Before
    fun setup() {
        viewModel = AdminDashboardViewModel(repository)
    }

    @Test
    fun `currentUserId returns ID when user is logged in`() {
        // Given
        val expectedId = "admin_user_123"
        every { repository.getCurrentUserId() } returns expectedId

        // When
        val actualId = viewModel.currentUserId

        // Then
        assertEquals(expectedId, actualId)
    }

    @Test
    fun `currentUserId returns null when user is logged out`() {
        // Given
        every { repository.getCurrentUserId() } returns null

        // When
        val actualId = viewModel.currentUserId

        // Then
        assertNull(actualId)
    }
}