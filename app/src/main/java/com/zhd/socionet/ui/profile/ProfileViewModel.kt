package com.zhd.socionet.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        // Optimization: Don't show loading if we already have this user's profile
        if (_uiState.value.userProfile?.id == userId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getUserProfile(userId)
            result.onSuccess { profile ->
                _uiState.update { it.copy(isLoading = false, userProfile = profile) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        val currentProfile = _uiState.value.userProfile ?: return
        val updatedProfile = currentProfile.copy(fullName = name, phoneNumber = phone)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            val result = repository.updateUserProfile(updatedProfile)

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, userProfile = updatedProfile, successMessage = "Profile Updated") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        val userId = _uiState.value.userProfile?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            val result = repository.uploadProfilePicture(userId, uri)
            
            result.onSuccess { url ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        userProfile = state.userProfile?.copy(profilePhotoUrl = url),
                        successMessage = "Profile picture updated successfully"
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            val result = repository.updatePassword(currentPass, newPass)

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Password Changed Successfully") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
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
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val displayFlatId: String?
        get() = userProfile?.assignedFlatId?.replace("_", " ")?.split(" ")?.lastOrNull()
}