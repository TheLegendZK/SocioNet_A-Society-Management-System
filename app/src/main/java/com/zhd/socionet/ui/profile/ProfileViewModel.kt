package com.zhd.socionet.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = repository.getUserProfile(userId)
            result.onSuccess { profile ->
                uiState = uiState.copy(isLoading = false, userProfile = profile)
            }.onFailure { e ->
                uiState = uiState.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        val currentProfile = uiState.userProfile ?: return
        val updatedProfile = currentProfile.copy(fullName = name, phoneNumber = phone)

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, successMessage = null, errorMessage = null)
            val result = repository.updateUserProfile(updatedProfile)

            if (result.isSuccess) {
                uiState = uiState.copy(isLoading = false, userProfile = updatedProfile, successMessage = "Profile Updated")
            } else {
                uiState = uiState.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, successMessage = null, errorMessage = null)
            val result = repository.updatePassword(currentPass, newPass)

            if (result.isSuccess) {
                uiState = uiState.copy(isLoading = false, successMessage = "Password Changed Successfully")
            } else {
                uiState = uiState.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onLogoutSuccess()
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)