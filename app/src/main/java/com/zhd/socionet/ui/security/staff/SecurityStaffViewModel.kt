package com.zhd.socionet.ui.security.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityStaffViewModel @Inject constructor(
    private val staffRepo: StaffRepository,
    private val gateRepo: GatePassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityStaffUiState())
    val uiState: StateFlow<SecurityStaffUiState> = _uiState.asStateFlow()

    fun loadStaff(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = staffRepo.getStaffList(societyId)
            _uiState.update { it.copy(isLoading = false, staffList = list) }
        }
    }

    fun markAttendance(staff: ServiceStaff, action: String) {
        viewModelScope.launch {
            gateRepo.logStaffEntry(staff.id, staff.name, action)
        }
    }
}

data class SecurityStaffUiState(
    val staffList: List<ServiceStaff> = emptyList(),
    val isLoading: Boolean = false
)