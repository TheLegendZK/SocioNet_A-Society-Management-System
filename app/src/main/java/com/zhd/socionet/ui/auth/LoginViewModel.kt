package com.zhd.socionet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String, onLoginSuccess: (UserProfile) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, resetPasswordMessage = null) }

            val result = repository.loginUser(email, pass)

            result.onSuccess { profile ->
                _uiState.update { it.copy(isLoading = false) }
                onLoginSuccess(profile)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(resetPasswordMessage = "Please enter your email address first.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, resetPasswordMessage = null) }
            val result = repository.sendPasswordResetEmail(email)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, resetPasswordMessage = "Password reset email sent. Check your inbox.") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, resetPasswordMessage = e.message ?: "Failed to send reset email.") }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, resetPasswordMessage = null) }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resetPasswordMessage: String? = null
)
