package com.zhd.socionet.ui.billing

import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.data.repository.BillingRepository
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

class BillingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: BillingRepository = mockk()
    private lateinit var viewModel: BillingViewModel

    @Before
    fun setup() {
        viewModel = BillingViewModel(repository)
    }

    @Test
    fun `loadBills calculates total outstanding correctly`() = runTest {
        // Given
        val flatId = "flat_101"
        val bills = listOf(
            Bill(id = "1", amountDue = 1000.0, isPaid = false), // Unpaid
            Bill(id = "2", amountDue = 2000.0, isPaid = false), // Unpaid
            Bill(id = "3", amountDue = 1500.0, isPaid = true)   // Paid (Should be ignored)
        )

        coEvery { repository.getBillsForFlat(flatId) } returns bills

        // When
        viewModel.loadBills(flatId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.bills.size)
        // Expected: 1000 + 2000 = 3000
        assertEquals(3000.0, state.totalOutstanding, 0.0)
    }

    @Test
    fun `payBill calls repository and reloads bills`() = runTest {
        // Given
        val billId = "bill_123"
        val userId = "user_456"
        val flatId = "flat_101"

        coEvery { repository.payBill(billId, userId) } returns Result.success(Unit)
        // Mock the reload call that happens after payment
        coEvery { repository.getBillsForFlat(flatId) } returns emptyList()

        // When
        viewModel.payBill(billId, userId, flatId)

        // Then
        coVerify(exactly = 1) { repository.payBill(billId, userId) }
        coVerify(exactly = 1) { repository.getBillsForFlat(flatId) }
    }

    @Test
    fun `generateBills updates admin message on success`() = runTest {
        // Given
        val societyId = "soc_1"
        val count = 15
        coEvery { repository.generateMonthlyBills(any(), any()) } returns Result.success(count)

        // When
        viewModel.generateBillsForCurrentMonth(societyId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.adminMessage!!.contains("Success"))
        assertTrue(state.adminMessage.contains("$count"))
    }

    @Test
    fun `generateBills updates admin message on failure`() = runTest {
        // Given
        val errorMsg = "Network error"
        coEvery { repository.generateMonthlyBills(any(), any()) } returns Result.failure(Exception(errorMsg))

        // When
        viewModel.generateBillsForCurrentMonth("soc_1")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error: $errorMsg", state.adminMessage)
    }
}