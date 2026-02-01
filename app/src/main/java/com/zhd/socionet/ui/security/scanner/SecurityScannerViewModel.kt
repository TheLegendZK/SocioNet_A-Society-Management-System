package com.zhd.socionet.ui.security.scanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityScannerViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    var uiState by mutableStateOf(ScannerUiState())
        private set

    // --- Actions ---

    fun onOtpEntered(otp: String) {
        if (otp.length == 6) {
            verifyPass(isQr = false, code = otp)
        }
    }

    fun onQrScanned(passId: String) {
        verifyPass(isQr = true, code = passId)
    }

    private fun verifyPass(isQr: Boolean, code: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, scanResult = null)

            // 1. Verify Validity
            val result = if (isQr) repository.verifyEntryByQr(code) else repository.verifyEntryByOtp(code)

            result.onSuccess { entry ->
                // 2. Mark as Checked In (Real World: The Guard doesn't need to click twice)
                markCheckIn(entry)
            }.onFailure {
                uiState = uiState.copy(
                    isLoading = false,
                    scanResult = ScanResult.Error(it.message ?: "Unknown Error")
                )
            }
        }
    }

    private fun markCheckIn(entry: GateEntry) {
        viewModelScope.launch {
            // Hardcoded guard ID for demo. Real app gets it from Auth.
            val checkInResult = repository.markEntryAsCheckedIn(entry.id, "guard_001")

            checkInResult.onSuccess {
                uiState = uiState.copy(
                    isLoading = false,
                    scanResult = ScanResult.Success(entry)
                )
                // Auto-reset screen after 3 seconds so guard is ready for next car
                delay(3000)
                resetScanner()
            }.onFailure {
                uiState = uiState.copy(
                    isLoading = false,
                    scanResult = ScanResult.Error("Database Error: Could not check in.")
                )
            }
        }
    }

    fun resetScanner() {
        uiState = ScannerUiState()
    }
}

// --- UI States ---
data class ScannerUiState(
    val isLoading: Boolean = false,
    val scanResult: ScanResult? = null
)

sealed class ScanResult {
    data class Success(val entry: GateEntry) : ScanResult()
    data class Error(val message: String) : ScanResult()
}