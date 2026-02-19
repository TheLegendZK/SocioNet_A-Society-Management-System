package com.zhd.socionet.ui.security.entries

import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ManualEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: GatePassRepository = mockk()
    private lateinit var viewModel: ManualEntryViewModel

    @Before
    fun setup() {
        viewModel = ManualEntryViewModel(repository)
    }

    @Test
    fun `logEntry creates correct gate pass`() = runTest {
        // Given
        val name = "Amazon"
        val flat = "A_101"
        val vehicle = "MH12AB1234"

        coEvery { repository.createGatePass(any()) } returns Result.success("pass_id")

        // When
        viewModel.logEntry(name, flat, vehicle)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.successMessage)

        // Verify the data passed to repository matches manual entry requirements
        coVerify {
            repository.createGatePass(match {
                it.visitorName == name &&
                        it.hostFlatId == flat &&
                        it.vehicleNumber == vehicle &&
                        it.visitorType == VisitorType.DELIVERY && // Should default to DELIVERY per ViewModel logic
                        it.status == EntryStatus.PENDING_APPROVAL
            })
        }
    }
}