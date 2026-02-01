package com.zhd.socionet.ui.admin.staff

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
class StaffRegistryViewModel @Inject constructor(
    private val repository: StaffRepository
) : ViewModel() {

    var uiState by mutableStateOf(StaffRegistryUiState())
        private set

    init {
        // Hardcoded for demo. In real app, get from Admin Profile.
        loadStaff("WbZ6qVoyNlMi1Quch8o3")
    }

    fun loadStaff(societyId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val staffList = repository.getStaffList(societyId)
            uiState = uiState.copy(isLoading = false, staffList = staffList)
        }
    }

    fun addStaff(name: String, role: String, phone: String) {
        viewModelScope.launch {
            val newStaff = ServiceStaff(
                name = name,
                role = role,
                phoneNumber = phone,
                societyId = "WbZ6qVoyNlMi1Quch8o3",
                isActive = true
            )

            val result = repository.addStaff(newStaff)
            if (result.isSuccess) {
                loadStaff("WbZ6qVoyNlMi1Quch8o3") // Refresh list
            }
        }
    }

    fun toggleStatus(staff: ServiceStaff) {
        viewModelScope.launch {
            val newStatus = !staff.isActive
            val result = repository.updateStaffStatus(staff.id, newStatus)
            if (result.isSuccess) {
                // Optimistic Update
                val updatedList = uiState.staffList.map {
                    if (it.id == staff.id) it.copy(isActive = newStatus) else it
                }
                uiState = uiState.copy(staffList = updatedList)
            }
        }
    }
}

data class StaffRegistryUiState(
    val isLoading: Boolean = false,
    val staffList: List<ServiceStaff> = emptyList()
)