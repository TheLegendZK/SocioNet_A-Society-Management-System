package com.zhd.socionet.ui.resident.gatepass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GatePassViewModel @Inject constructor(
    private val repository: GatePassRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    var uiState by mutableStateOf(GatePassUiState())
        private set

    // --- REAL TIME LOADER ---
    fun loadPendingRequests(flatId: String) {
        viewModelScope.launch {
            // This stays open and updates whenever Firestore changes
            repository.getPendingRequestsFlow(flatId).collectLatest { requests ->
                uiState = uiState.copy(pendingRequests = requests)
            }
        }
    }

    fun loadHistory(flatId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val history = repository.getPastPasses(flatId)
            uiState = uiState.copy(isLoading = false, visitorHistory = history)
        }
    }

    // --- Actions ---

    fun onVisitorNameChanged(name: String) {
        uiState = uiState.copy(visitorName = name)
    }

    fun onVisitorTypeChanged(type: VisitorType) {
        uiState = uiState.copy(visitorType = type)
    }

    fun generatePass(hostFlatId: String, hostUserId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            if (uiState.visitorName.isBlank()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Visitor Name is required")
                return@launch
            }

            val otp = Random.nextInt(100000, 999999).toString()
            val entry = GateEntry(
                visitorName = uiState.visitorName,
                visitorType = uiState.visitorType,
                hostFlatId = hostFlatId,
                hostUserId = hostUserId,
                entryCode = otp,
                entryTime = Timestamp(Date()),
                status = EntryStatus.APPROVED // Pre-approved
            )

            val result = repository.createGatePass(entry)
            result.onSuccess { passId ->
                uiState = uiState.copy(isLoading = false, isSuccess = true, generatedPassId = passId, generatedOtp = otp)
            }.onFailure {
                uiState = uiState.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    fun respondToRequest(entry: GateEntry, accept: Boolean) {
        viewModelScope.launch {
            val status = if (accept) EntryStatus.APPROVED else EntryStatus.DENIED
            val result = repository.respondToRequest(entry.id, status)
            // No need to manually update list, the Flow will auto-update!
        }
    }

    fun loadLinkedStaffAndLogs(societyId: String, flatId: String) {
        viewModelScope.launch {
            // 1. Get all staff in society
            val allStaff = staffRepository.getStaffList(societyId)

            // 2. Filter only those linked to THIS flat
            val myStaff = allStaff.filter { it.employerFlatIds.contains(flatId) }

            if (myStaff.isNotEmpty()) {
                val myStaffIds = myStaff.map { it.id }

                // 3. Start observing their logs
                repository.getStaffLogsFlow(myStaffIds).collectLatest { logs ->
                    uiState = uiState.copy(
                        linkedStaff = myStaff,
                        staffLogs = logs
                    )
                }
            } else {
                uiState = uiState.copy(linkedStaff = emptyList())
            }
        }
    }

    fun resetState() {
        uiState = uiState.copy(isSuccess = false, generatedPassId = null, generatedOtp = null, visitorName = "")
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

    val linkedStaff: List<ServiceStaff> = emptyList(), // List of your maids/drivers
    val staffLogs: List<GateEntry> = emptyList()       // Their entry history
)