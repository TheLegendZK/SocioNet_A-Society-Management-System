package com.zhd.socionet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        loadSocieties()
    }

    private fun loadSocieties() {
        viewModelScope.launch {
            val list = repository.fetchSocieties()
            _uiState.update { it.copy(societies = list) }
        }
    }

    fun onSocietySelected(id: String) {
        _uiState.update { it.copy(selectedSocietyId = id, selectedWing = "", selectedFlatId = "") }
        viewModelScope.launch {
            val wings = repository.fetchWings(id)
            _uiState.update { it.copy(wings = wings) }
        }
    }

    fun onWingSelected(wing: String) {
        _uiState.update { it.copy(selectedWing = wing, selectedFlatId = "") }
        viewModelScope.launch {
            val flats = repository.fetchFlats(_uiState.value.selectedSocietyId, wing)
            _uiState.update { it.copy(flats = flats) }
        }
    }

    fun onFlatSelected(id: String) {
        _uiState.update { it.copy(selectedFlatId = id) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun registerUser(email: String, pass: String, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            if (_uiState.value.selectedFlatId.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please select a flat first.") }
                return@launch
            }

            if (_uiState.value.selectedRole == UserRole.OWNER) {
                val currentOwner = repository.checkFlatOccupancy(_uiState.value.selectedFlatId)
                if (currentOwner != null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Security Alert: This flat already has a registered Owner. Please join as Family Member."
                    ) }
                    return@launch
                }
            }

            val newProfile = UserProfile(
                fullName = name,
                email = email,
                phoneNumber = phone,
                roles = listOf(_uiState.value.selectedRole),
                assignedFlatId = _uiState.value.selectedFlatId,
                isVerified = false
            )

            val result = repository.registerUser(email, pass, newProfile)

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }
}

data class RegisterUiState(
    val societies: List<Pair<String, String>> = emptyList(),
    val wings: List<String> = emptyList(),
    val flats: List<Pair<String, String>> = emptyList(),
    val selectedSocietyId: String = "",
    val selectedWing: String = "",
    val selectedFlatId: String = "",
    val selectedRole: UserRole = UserRole.OWNER,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)