package com.zhd.socionet.ui.security.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.GatePassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityLogViewModel @Inject constructor(
    private val gateRepository: GatePassRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityLogUiState())
    val uiState: StateFlow<SecurityLogUiState> = _uiState.asStateFlow()

    fun loadLogs(societyId: String) {
        if (societyId.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            gateRepository.getSecurityLogsFlow(societyId).collectLatest { logs ->
                _uiState.update { it.copy(isLoading = false, logs = logs) }
            }
        }
    }
}

data class SecurityLogUiState(
    val logs: List<GateEntry> = emptyList(),
    val isLoading: Boolean = false
)
