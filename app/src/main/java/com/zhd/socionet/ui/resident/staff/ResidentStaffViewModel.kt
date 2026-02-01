package com.zhd.socionet.ui.resident.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentStaffViewModel @Inject constructor(
    private val repository: StaffRepository
) : ViewModel() {

    var uiState by mutableStateOf(ResidentStaffUiState())
        private set

    fun loadStaff(societyId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val list = repository.getStaffList(societyId)
            uiState = uiState.copy(isLoading = false, allStaff = list)
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
                val updatedList = uiState.allStaff.map { item ->
                    if (item.id == staff.id) {
                        val newEmployers = if (isCurrentlyLinked) {
                            item.employerFlatIds - flatId
                        } else {
                            item.employerFlatIds + flatId
                        }
                        item.copy(employerFlatIds = newEmployers)
                    } else item
                }
                uiState = uiState.copy(allStaff = updatedList)
            }
        }
    }
}

data class ResidentStaffUiState(
    val isLoading: Boolean = false,
    val allStaff: List<ServiceStaff> = emptyList()
)