package com.zhd.socionet.ui.security.entries

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.GatePassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val gateRepository: GatePassRepository,
    private val authRepository: AuthRepository // To find flat ID from name
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var successMessage by mutableStateOf<String?>(null)

    // Simplified: In real app, we search for flat ID. Here assuming Guard knows Flat ID or we map it.
    // For demo, we will ask Guard to type Flat ID (e.g. royal_palms_mumbai_A_101)
    fun logEntry(name: String, flatId: String, vehicle: String) {
        viewModelScope.launch {
            isLoading = true
            val entry = GateEntry(
                visitorName = name,
                visitorType = VisitorType.DELIVERY, // Default
                vehicleNumber = vehicle,
                hostFlatId = flatId,
                entryTime = Timestamp(Date()),
                status = EntryStatus.PENDING_APPROVAL // <--- CRITICAL
            )

            val result = gateRepository.createGatePass(entry)
            isLoading = false
            if (result.isSuccess) successMessage = "Request Sent to Resident!"
        }
    }
}