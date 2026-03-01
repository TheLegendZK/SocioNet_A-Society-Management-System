package com.zhd.socionet.ui.resident.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.data.model.ComplaintScope
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val repository: ComplaintRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComplaintUiState())
    val uiState: StateFlow<ComplaintUiState> = _uiState.asStateFlow()

    fun loadComplaints(flatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = repository.getComplaintsForFlat(flatId)
            _uiState.update { it.copy(isLoading = false, complaints = list) }
        }
    }

    fun submitComplaint(
        title: String,
        description: String,
        category: ComplaintCategory,
        flatId: String,
        userId: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Denormalization: Get user profile to store name and flat number
            val profile = authRepository.getUserProfile(userId)

            val complaint = Complaint(
                title = title,
                description = description,
                category = category,
                scope = ComplaintScope.PERSONAL,
                flatId = flatId,
                flatNumber = profile?.flatNumber,
                raisedByUserId = userId,
                raisedByUserName = profile?.fullName,
                status = "OPEN"
            )

            val result = repository.fileComplaint(complaint)

            if (result.isSuccess) {
                val updatedList = repository.getComplaintsForFlat(flatId)
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    complaints = updatedList
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to submit complaint"
                ) }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}

data class ComplaintUiState(
    val isLoading: Boolean = false,
    val complaints: List<Complaint> = emptyList(),
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)