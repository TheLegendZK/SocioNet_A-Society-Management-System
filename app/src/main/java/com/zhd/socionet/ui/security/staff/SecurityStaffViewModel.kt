package com.zhd.socionet.ui.security.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.GatePassRepository
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityStaffViewModel @Inject constructor(
    private val staffRepo: StaffRepository,
    private val gateRepo: GatePassRepository
) : ViewModel() {

    var staffList by mutableStateOf<List<ServiceStaff>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        loadStaff()
    }

    private fun loadStaff() {
        viewModelScope.launch {
            isLoading = true
            // Hardcoded society
            staffList = staffRepo.getStaffList("WbZ6qVoyNlMi1Quch8o3")
            isLoading = false
        }
    }

    fun markAttendance(staff: ServiceStaff, action: String) {
        viewModelScope.launch {
            gateRepo.logStaffEntry(staff.id, staff.name, action)
            // In a real app, we'd show a Snackbar here
        }
    }
}