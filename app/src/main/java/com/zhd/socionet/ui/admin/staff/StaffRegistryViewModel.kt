package com.zhd.socionet.ui.admin.staff

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
class StaffRegistryViewModel @Inject constructor(
    private val repository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffRegistryUiState())
    val uiState: StateFlow<StaffRegistryUiState> = _uiState.asStateFlow()

    fun loadStaff(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val staffList = repository.getStaffList(societyId)
            _uiState.update { it.copy(isLoading = false, staffList = staffList) }
        }
    }

    fun addStaff(name: String, role: String, phone: String, societyId: String) {
        viewModelScope.launch {
            val newStaff = ServiceStaff(
                name = name,
                role = role,
                phoneNumber = phone,
                societyId = societyId,
                isActive = true
            )

            val result = repository.addStaff(newStaff)
            if (result.isSuccess) {
                loadStaff(societyId)
            }
        }
    }

    fun toggleStatus(staff: ServiceStaff) {
        viewModelScope.launch {
            val newStatus = !staff.isActive
            val result = repository.updateStaffStatus(staff.id, newStatus)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updatedList = state.staffList.map {
                        if (it.id == staff.id) it.copy(isActive = newStatus) else it
                    }
                    state.copy(staffList = updatedList)
                }
            }
        }
    }
}

data class StaffRegistryUiState(
    val isLoading: Boolean = false,
    val staffList: List<ServiceStaff> = emptyList()
)