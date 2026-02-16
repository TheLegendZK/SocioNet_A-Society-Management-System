package com.zhd.socionet.ui.admin.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminComplaintViewModel @Inject constructor(
    private val repository: ComplaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminComplaintUiState())
    val uiState: StateFlow<AdminComplaintUiState> = _uiState.asStateFlow()

    init {
        loadAllComplaints()
    }

    fun loadAllComplaints() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allComplaints = repository.getAllComplaints()
            _uiState.update { it.copy(isLoading = false, complaints = allComplaints) }
        }
    }

    fun updateStatus(complaint: Complaint, newStatus: String) {
        viewModelScope.launch {
            val result = repository.updateStatus(complaint.id, newStatus)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updatedList = state.complaints.map {
                        if (it.id == complaint.id) it.copy(status = newStatus) else it
                    }
                    state.copy(complaints = updatedList)
                }
            }
        }
    }
}

data class AdminComplaintUiState(
    val isLoading: Boolean = false,
    val complaints: List<Complaint> = emptyList()
)