package com.zhd.socionet.ui.security.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityScannerViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onOtpEntered(otp: String) {
        if (otp.length == 6 && !_uiState.value.isLoading) {
            verifyPass(isQr = false, code = otp)
        }
    }

    fun onQrScanned(passId: String) {
        if (_uiState.value.isLoading || _uiState.value.scanResult != null) return
        verifyPass(isQr = true, code = passId)
    }

    private fun verifyPass(isQr: Boolean, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scanResult = null) }

            val result = if (isQr) repository.verifyEntryByQr(code) else repository.verifyEntryByOtp(code)

            result.onSuccess { entry ->
                markCheckIn(entry)
            }.onFailure {exception ->
                _uiState.update { it.copy(
                    isLoading = false,
                    scanResult = ScanResult.Error(exception.message ?: "Unknown Error")
                ) }
            }
        }
    }

    private fun markCheckIn(entry: GateEntry) {
        viewModelScope.launch {
            val guardId = auth.currentUser?.uid ?: "unknown_guard"
            val checkInResult = repository.markEntryAsCheckedIn(entry.id, guardId)

            checkInResult.onSuccess {
                _uiState.update { it.copy(
                    isLoading = false,
                    scanResult = ScanResult.Success(entry)
                ) }
                delay(3000)
                resetScanner()
            }.onFailure {
                _uiState.update { it.copy(
                    isLoading = false,
                    scanResult = ScanResult.Error("Database Error: Could not check in.")
                ) }
            }
        }
    }

    fun resetScanner() {
        _uiState.update { ScannerUiState() }
    }
}

data class ScannerUiState(
    val isLoading: Boolean = false,
    val scanResult: ScanResult? = null
)

sealed class ScanResult {
    data class Success(val entry: GateEntry) : ScanResult()
    data class Error(val message: String) : ScanResult()
}
