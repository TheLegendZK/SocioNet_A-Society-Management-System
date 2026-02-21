package com.zhd.socionet.ui.resident.gatepass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.zhd.socionet.core.utils.OtpGenerator
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GatePassViewModel @Inject constructor(
    private val repository: GatePassRepository,
    private val staffRepository: StaffRepository,
    private val otpGenerator: OtpGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(GatePassUiState())
    val uiState: StateFlow<GatePassUiState> = _uiState.asStateFlow()

    private var pendingJob: Job? = null
    private var historyJob: Job? = null
    private var staffJob: Job? = null

    fun loadPendingRequests(flatId: String) {
        pendingJob?.cancel()
        pendingJob = viewModelScope.launch {
            repository.getPendingRequestsFlow(flatId).collectLatest { requests ->
                _uiState.update { it.copy(pendingRequests = requests) }
            }
        }
    }

    fun loadHistory(flatId: String) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getPastPassesFlow(flatId).collectLatest { history ->
                _uiState.update { it.copy(isLoading = false, visitorHistory = history) }
            }
        }
    }

    fun onVisitorNameChanged(name: String) {
        _uiState.update { it.copy(visitorName = name) }
    }

    fun onVisitorTypeChanged(type: VisitorType) {
        _uiState.update { it.copy(visitorType = type) }
    }

    fun generatePass(hostFlatId: String, hostUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (_uiState.value.visitorName.isBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Visitor Name is required") }
                return@launch
            }

            val otp = otpGenerator.generateOtp()
            val entry = GateEntry(
                visitorName = _uiState.value.visitorName,
                visitorType = _uiState.value.visitorType,
                hostFlatId = hostFlatId,
                hostUserId = hostUserId,
                entryCode = otp,
                entryTime = Timestamp(Date()),
                status = EntryStatus.APPROVED
            )

            val result = repository.createGatePass(entry)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true, generatedPassId = result.getOrNull(), generatedOtp = otp) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun respondToRequest(entry: GateEntry, accept: Boolean) {
        viewModelScope.launch {
            val status = if (accept) EntryStatus.APPROVED else EntryStatus.DENIED
            repository.respondToRequest(entry.id, status)
        }
    }

    fun loadLinkedStaffAndLogs(societyId: String, flatId: String) {
        staffJob?.cancel()
        staffJob = viewModelScope.launch {
            val allStaff = staffRepository.getStaffList(societyId)
            val myStaff = allStaff.filter { it.employerFlatIds.contains(flatId) }

            if (myStaff.isNotEmpty()) {
                val myStaffIds = myStaff.map { it.id }
                repository.getStaffLogsFlow(myStaffIds).collectLatest { logs ->
                    _uiState.update { it.copy(linkedStaff = myStaff, staffLogs = logs) }
                }
            } else {
                _uiState.update { it.copy(linkedStaff = emptyList(), staffLogs = emptyList()) }
            }
        }
    }

    fun resetState() {
        _uiState.update { it.copy(isSuccess = false, generatedPassId = null, generatedOtp = null, visitorName = "") }
    }
}

data class GatePassUiState(
    val visitorName: String = "",
    val visitorType: VisitorType = VisitorType.GUEST,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val generatedPassId: String? = null,
    val generatedOtp: String? = null,
    val errorMessage: String? = null,
    val pendingRequests: List<GateEntry> = emptyList(),
    val visitorHistory: List<GateEntry> = emptyList(),
    val linkedStaff: List<ServiceStaff> = emptyList(),
    val staffLogs: List<GateEntry> = emptyList()
)
