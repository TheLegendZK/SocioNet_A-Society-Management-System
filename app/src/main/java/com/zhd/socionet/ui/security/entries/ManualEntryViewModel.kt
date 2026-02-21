package com.zhd.socionet.ui.security.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.GatePassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val gateRepository: GatePassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

    fun logEntry(name: String, flatId: String, vehicle: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Find the user associated with the flat
            val user = gateRepository.getUserByFlat(flatId)
            val hostUserId = user?.id ?: ""

            val entry = GateEntry(
                visitorName = name,
                visitorType = VisitorType.DELIVERY,
                vehicleNumber = vehicle,
                hostFlatId = flatId,
                hostUserId = hostUserId,
                entryTime = Timestamp(Date()),
                status = EntryStatus.PENDING_APPROVAL
            )

            val result = gateRepository.createGatePass(entry)
            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                if (hostUserId.isNotBlank()) {
                    _uiState.update { it.copy(successMessage = "Request Sent to Resident!") }
                } else {
                    _uiState.update { it.copy(successMessage = "Entry Logged! (No Resident found for this Flat)") }
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

data class ManualEntryUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null
)