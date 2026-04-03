package com.zhd.socionet.ui.security.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.Flat
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
    private val gateRepository: GatePassRepository,
    private val authRepository: com.zhd.socionet.data.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

    private var allFlats: List<Flat> = emptyList()
    private var guardSocietyId: String = ""

    init {
        fetchFlats()
        loadGuardSociety()
    }

    private fun loadGuardSociety() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val profile = authRepository.getUserProfile(userId)
                guardSocietyId = profile?.societyId ?: ""
            }
        }
    }

    private fun fetchFlats() {
        viewModelScope.launch {
            val result = gateRepository.getAllFlats()
            if (result.isSuccess) {
                allFlats = result.getOrDefault(emptyList())
                val wings = allFlats.map { it.wing }.distinct().sorted()
                _uiState.update { it.copy(wings = wings) }
            }
        }
    }

    fun onWingSelected(wing: String) {
        val flatsInWing = allFlats
            .filter { it.wing == wing }
            .map { it.flatNumber }
            .sortedBy { it.toIntOrNull() ?: 0 }

        _uiState.update { 
            it.copy(
                selectedWing = wing, 
                flatsInWing = flatsInWing,
                selectedFlat = "" // Reset selected flat when wing changes
            ) 
        }
    }

    fun onFlatSelected(flat: String) {
        _uiState.update { it.copy(selectedFlat = flat) }
    }

    fun logEntry(name: String, vehicle: String) {
        val wing = _uiState.value.selectedWing
        val flatNumber = _uiState.value.selectedFlat
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val targetFlat = allFlats.find { it.wing == wing && it.flatNumber == flatNumber }
            val hostFlatId = targetFlat?.id ?: "${wing}_${flatNumber}"
            
            val user = gateRepository.getUserByFlat(hostFlatId)
            val hostUserId = user?.id ?: ""

            val entry = GateEntry(
                visitorName = name,
                societyId = guardSocietyId,
                visitorType = VisitorType.DELIVERY,
                vehicleNumber = vehicle,
                hostFlatId = hostFlatId,
                hostFlatNumber = targetFlat?.flatNumber ?: flatNumber,
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
    val successMessage: String? = null,
    val wings: List<String> = emptyList(),
    val flatsInWing: List<String> = emptyList(),
    val selectedWing: String = "",
    val selectedFlat: String = ""
)
