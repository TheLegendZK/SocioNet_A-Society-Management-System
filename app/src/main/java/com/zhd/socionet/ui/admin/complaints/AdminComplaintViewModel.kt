package com.zhd.socionet.ui.admin.complaints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminComplaintViewModel @Inject constructor(
    private val repository: ComplaintRepository
) : ViewModel() {

    var uiState by mutableStateOf(AdminComplaintUiState())
        private set

    init {
        loadAllComplaints()
    }

    fun loadAllComplaints() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val allComplaints = repository.getAllComplaints()
            uiState = uiState.copy(isLoading = false, complaints = allComplaints)
        }
    }

    fun updateStatus(complaint: Complaint, newStatus: String) {
        viewModelScope.launch {
            val result = repository.updateStatus(complaint.id, newStatus)
            if (result.isSuccess) {
                // Optimistic Update: Update list locally to feel instant
                val updatedList = uiState.complaints.map {
                    if (it.id == complaint.id) it.copy(status = newStatus) else it
                }
                uiState = uiState.copy(complaints = updatedList)
            }
        }
    }
}

data class AdminComplaintUiState(
    val isLoading: Boolean = false,
    val complaints: List<Complaint> = emptyList()
)