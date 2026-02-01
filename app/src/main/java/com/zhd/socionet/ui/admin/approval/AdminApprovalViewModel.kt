package com.zhd.socionet.ui.admin.approval

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.AdminUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminApprovalViewModel @Inject constructor(
    private val repository: AdminUserRepository
) : ViewModel() {

    // Public getter, private setter (Fixes the visibility issue pattern)
    var uiState by mutableStateOf(AdminApprovalUiState())
        private set

    init {
        loadPendingRequests()
    }

    private fun loadPendingRequests() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            // Hardcoded Society ID for now. In real app, get from Admin's profile.
            val users = repository.getPendingUsers("WbZ6qVoyNlMi1Quch8o3")
            uiState = uiState.copy(isLoading = false, pendingUsers = users)
        }
    }

    fun approveUser(user: UserProfile) {
        viewModelScope.launch {
            val result = repository.approveUser(user.id)
            if (result.isSuccess) {
                // Remove from list locally to update UI instantly
                uiState = uiState.copy(
                    pendingUsers = uiState.pendingUsers.filter { it.id != user.id },
                    message = "Approved ${user.fullName}"
                )
            } else {
                uiState = uiState.copy(message = "Failed to approve: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun rejectUser(user: UserProfile) {
        viewModelScope.launch {
            val result = repository.rejectUser(user.id)
            if (result.isSuccess) {
                uiState = uiState.copy(
                    pendingUsers = uiState.pendingUsers.filter { it.id != user.id },
                    message = "Rejected ${user.fullName}"
                )
            } else {
                uiState = uiState.copy(message = "Failed to reject")
            }
        }
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }
}

data class AdminApprovalUiState(
    val isLoading: Boolean = false,
    val pendingUsers: List<UserProfile> = emptyList(),
    val message: String? = null
)