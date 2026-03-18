package com.zhd.socionet.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.data.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    fun loadBills(flatId: String) {
        // Optimization: Don't trigger loading flash if data is already present for this flat
        if (_uiState.value.bills.isNotEmpty() && _uiState.value.bills.all { it.flatId == flatId }) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bills = repository.getBillsForFlat(flatId)
            val outstanding = bills.filter { !it.isPaid }.sumOf { it.amountDue }
            _uiState.update { it.copy(isLoading = false, bills = bills, totalOutstanding = outstanding) }
        }
    }

    fun loadAllBillsForAdmin(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bills = repository.getAllBills(societyId)
            _uiState.update { it.copy(isLoading = false, bills = bills) }
        }
    }

    fun payBill(billId: String, userId: String, flatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.payBill(billId, userId)
            
            result.onSuccess {
                // Force reload after payment
                val bills = repository.getBillsForFlat(flatId)
                val outstanding = bills.filter { !it.isPaid }.sumOf { it.amountDue }
                _uiState.update { it.copy(isLoading = false, bills = bills, totalOutstanding = outstanding, adminMessage = "Payment Successful") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, adminMessage = "Payment Failed: ${e.message}") }
            }
        }
    }

    fun generateBillsForCurrentMonth(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, adminMessage = null) }

            val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val result = repository.generateMonthlyBills(societyId, currentMonth)

            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        adminMessage = "Success! Generated $count bills for $currentMonth."
                    )
                }
                loadAllBillsForAdmin(societyId) // Refresh admin list
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        adminMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(adminMessage = null) }
    }
}

data class BillingUiState(
    val isLoading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val adminMessage: String? = null
)
