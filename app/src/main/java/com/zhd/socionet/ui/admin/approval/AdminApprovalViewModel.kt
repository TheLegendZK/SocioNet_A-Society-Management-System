package com.zhd.socionet.ui.admin.approval

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.AdminUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminApprovalViewModel @Inject constructor(
    private val repository: AdminUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminApprovalUiState())
    val uiState: StateFlow<AdminApprovalUiState> = _uiState.asStateFlow()

    fun loadPendingRequests(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val users = repository.getPendingUsers(societyId)
            _uiState.update { it.copy(isLoading = false, pendingUsers = users) }
        }
    }

    fun approveUser(user: UserProfile) {
        viewModelScope.launch {
            val result = repository.approveUser(user)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        pendingUsers = state.pendingUsers.filter { it.id != user.id },
                        message = "Approved ${user.fullName}"
                    )
                }
            } else {
                _uiState.update { it.copy(message = "Failed to approve: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun rejectUser(user: UserProfile) {
        viewModelScope.launch {
            val result = repository.rejectUser(user.id)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        pendingUsers = state.pendingUsers.filter { it.id != user.id },
                        message = "Rejected ${user.fullName}"
                    )
                }
            } else {
                _uiState.update { it.copy(message = "Failed to reject") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class AdminApprovalUiState(
    val isLoading: Boolean = false,
    val pendingUsers: List<UserProfile> = emptyList(),
    val message: String? = null
)
