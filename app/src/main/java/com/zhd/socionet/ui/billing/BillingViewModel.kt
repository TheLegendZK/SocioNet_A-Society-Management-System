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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bills = repository.getBillsForFlat(flatId)
            val outstanding = bills.filter { !it.isPaid }.sumOf { it.amountDue }
            _uiState.update { it.copy(isLoading = false, bills = bills, totalOutstanding = outstanding) }
        }
    }

    fun payBill(billId: String, userId: String, flatId: String) {
        viewModelScope.launch {
            repository.payBill(billId, userId)
            loadBills(flatId) // Refresh
        }
    }

    fun generateBillsForCurrentMonth(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, adminMessage = null) }

            // FIX: Removed fake delay and implemented real repository call
            val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val result = repository.generateMonthlyBills(societyId, currentMonth)

            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        adminMessage = "Success! Generated $count bills for $currentMonth."
                    )
                }
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
}

data class BillingUiState(
    val isLoading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val adminMessage: String? = null
)
