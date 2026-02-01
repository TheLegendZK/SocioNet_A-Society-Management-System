package com.zhd.socionet.ui.resident.complaints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.data.model.ComplaintScope
import com.zhd.socionet.data.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val repository: ComplaintRepository
) : ViewModel() {

    var uiState by mutableStateOf(ComplaintUiState())
        private set

    fun loadComplaints(flatId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val list = repository.getComplaintsForFlat(flatId)
            uiState = uiState.copy(isLoading = false, complaints = list)
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
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val complaint = Complaint(
                title = title,
                description = description,
                category = category,
                scope = ComplaintScope.PERSONAL, // Defaulting to Personal for now
                flatId = flatId,
                raisedByUserId = userId,
                status = "OPEN"
            )

            val result = repository.fileComplaint(complaint)

            if (result.isSuccess) {
                // Refresh list and close dialog
                val updatedList = repository.getComplaintsForFlat(flatId)
                uiState = uiState.copy(
                    isLoading = false,
                    isSuccess = true,
                    complaints = updatedList
                )
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Failed to submit complaint"
                )
            }
        }
    }

    fun resetSuccess() {
        uiState = uiState.copy(isSuccess = false)
    }
}

data class ComplaintUiState(
    val isLoading: Boolean = false,
    val complaints: List<Complaint> = emptyList(),
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)