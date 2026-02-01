package com.zhd.socionet.ui.billing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.data.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: BillingRepository,
) : ViewModel() {

    var uiState by mutableStateOf(BillingUiState())
        private set

    fun loadBills(flatId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val bills = repository.getBillsForFlat(flatId)
            val pendingTotal = bills.filter { !it.isPaid }.sumOf { it.amountDue }
            uiState = uiState.copy(isLoading = false, bills = bills, totalOutstanding = pendingTotal)
        }
    }

    fun payBill(bill: Bill, userId: String) {
        viewModelScope.launch {
            val result = repository.payBill(bill.id, userId)
            if (result.isSuccess) loadBills(bill.flatId)
        }
    }

    fun generateBillsForCurrentMonth(societyId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val result = repository.generateMonthlyBills(societyId, currentMonth)
            result.onSuccess { count -> uiState = uiState.copy(isLoading = false, adminMessage = "Generated $count bills for $currentMonth") }
                .onFailure { uiState = uiState.copy(isLoading = false, adminMessage = "Ready for billing") }
        }
    }
}

data class BillingUiState(
    val isLoading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val adminMessage: String? = null
)