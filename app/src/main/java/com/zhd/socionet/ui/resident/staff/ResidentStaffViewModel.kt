package com.zhd.socionet.ui.resident.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentStaffViewModel @Inject constructor(
    private val repository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResidentStaffUiState())
    val uiState: StateFlow<ResidentStaffUiState> = _uiState.asStateFlow()

    fun loadStaff(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = repository.getStaffList(societyId)
            _uiState.update { it.copy(isLoading = false, allStaff = list) }
        }
    }

    fun toggleLink(staff: ServiceStaff, flatId: String) {
        viewModelScope.launch {
            val isCurrentlyLinked = staff.employerFlatIds.contains(flatId)

            val result = if (isCurrentlyLinked) {
                repository.unlinkStaffFromFlat(staff.id, flatId)
            } else {
                repository.linkStaffToFlat(staff.id, flatId)
            }

            if (result.isSuccess) {
                // Optimistic Update: Update the local list instantly
                val updatedList = _uiState.value.allStaff.map { item ->
                    if (item.id == staff.id) {
                        val newEmployers = if (isCurrentlyLinked) {
                            item.employerFlatIds - flatId
                        } else {
                            item.employerFlatIds + flatId
                        }
                        item.copy(employerFlatIds = newEmployers)
                    } else item
                }
                _uiState.update { it.copy(allStaff = updatedList) }
            }
        }
    }
}

data class ResidentStaffUiState(
    val isLoading: Boolean = false,
    val allStaff: List<ServiceStaff> = emptyList()
)
