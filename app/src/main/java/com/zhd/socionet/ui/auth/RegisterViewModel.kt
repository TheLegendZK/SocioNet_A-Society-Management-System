package com.zhd.socionet.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    init {
        loadSocieties()
    }

    private fun loadSocieties() {
        viewModelScope.launch {
            val list = repository.fetchSocieties()
            uiState = uiState.copy(societies = list)
        }
    }

    fun onSocietySelected(id: String) {
        uiState = uiState.copy(selectedSocietyId = id, selectedWing = "", selectedFlatId = "")
        viewModelScope.launch {
            val wings = repository.fetchWings(id)
            uiState = uiState.copy(wings = wings)
        }
    }

    fun onWingSelected(wing: String) {
        uiState = uiState.copy(selectedWing = wing, selectedFlatId = "")
        viewModelScope.launch {
            val flats = repository.fetchFlats(uiState.selectedSocietyId, wing)
            uiState = uiState.copy(flats = flats)
        }
    }

    fun onFlatSelected(id: String) {
        uiState = uiState.copy(selectedFlatId = id)
    }

    fun onRoleSelected(role: UserRole) {
        uiState = uiState.copy(selectedRole = role)
    }

    fun registerUser(email: String, pass: String, name: String, phone: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            if (uiState.selectedFlatId.isEmpty()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Please select a flat first.")
                return@launch
            }

            // 1. Validation: "The Real World Check"
            if (uiState.selectedRole == UserRole.OWNER) {
                val currentOwner = repository.checkFlatOccupancy(uiState.selectedFlatId)
                if (currentOwner != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Security Alert: This flat already has a registered Owner. Please join as Family Member."
                    )
                    return@launch
                }
            }

            // 2. Create Profile Object
            val newProfile = UserProfile(
                fullName = name,
                email = email,
                phoneNumber = phone,
                roles = listOf(uiState.selectedRole),
                assignedFlatId = uiState.selectedFlatId,
                isVerified = false // Always false initially. Admin must approve.
            )

            // 3. Submit
            val result = repository.registerUser(email, pass, newProfile)

            result.onSuccess {
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            }.onFailure {
                uiState = uiState.copy(isLoading = false, errorMessage = it.message)
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