package com.zhd.socionet.ui.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.SocietyRepository
import com.zhd.socionet.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocietyDirectoryViewModel @Inject constructor(
    private val repository: SocietyRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocietyDirectoryUiState())
    val uiState: StateFlow<SocietyDirectoryUiState> = _uiState.asStateFlow()

    fun setFlatId(flatId: String) {
        _uiState.update { it.copy(currentFlatId = flatId) }
    }

    fun loadDirectory(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val residentsResult = repository.getSocietyResidents(societyId)
            val staffResult = staffRepository.getStaffList(societyId)

            val sortedUsers = residentsResult.getOrNull()?.sortedBy { it.assignedFlatId } ?: emptyList()
            
            _uiState.update { it.copy(
                isLoading = false,
                residents = sortedUsers,
                serviceStaff = staffResult,
                errorMessage = residentsResult.exceptionOrNull()?.message
            ) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun removeUser(userId: String, societyId: String) {
        viewModelScope.launch {
            val result = repository.removeUserFromSociety(userId)
            if (result.isSuccess) {
                loadDirectory(societyId)
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to remove user") }
            }
        }
    }

    fun removeStaff(staffId: String, societyId: String) {
        // Technically, updating isActive = false or deleting the document.
        // Let's just update isActive to false to simulate "removal" to preserve history.
        viewModelScope.launch {
            val result = staffRepository.updateStaffStatus(staffId, false)
            if (result.isSuccess) {
                loadDirectory(societyId)
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to remove staff") }
            }
        }
    }

    fun addStaffMember(societyId: String, name: String, phone: String, role: String) {
        viewModelScope.launch {
            val newStaff = ServiceStaff(
                name = name,
                role = role,
                phoneNumber = phone,
                societyId = societyId,
                isActive = true
            )
            val result = staffRepository.addStaff(newStaff)
            if (result.isSuccess) {
                loadDirectory(societyId)
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to add staff") }
            }
        }
    }

    fun toggleStaffLink(staff: ServiceStaff, flatId: String, societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isLinked = staff.employerFlatIds.contains(flatId)
            val result = if (isLinked) {
                staffRepository.unlinkStaffFromFlat(staff.id, flatId)
            } else {
                staffRepository.linkStaffToFlat(staff.id, flatId)
            }
            if (result.isSuccess) {
                loadDirectory(societyId)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to update staff link") }
            }
        }
    }
}

data class SocietyDirectoryUiState(
    val isLoading: Boolean = false,
    val residents: List<UserProfile> = emptyList(),
    val serviceStaff: List<ServiceStaff> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val currentFlatId: String = "",
    val errorMessage: String? = null,
) {
    val filteredResidents: List<UserProfile>
        get() {
            val roleFiltered = when (selectedTab) {
                1 -> residents.filter { it.roles.contains(UserRole.SECURITY_GUARD) }
                else -> residents.filter { !it.roles.contains(UserRole.SECURITY_GUARD) && !it.roles.contains(UserRole.SERVICE_STAFF) }
            }

            return if (searchQuery.isBlank()) {
                roleFiltered
            } else {
                roleFiltered.filter {
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                            (it.assignedFlatId ?: "").contains(searchQuery, ignoreCase = true)
                }
            }
        }
        
    val filteredStaff: List<ServiceStaff>
        get() {
            val activeStaff = serviceStaff.filter { it.isActive }
            val searched = if (searchQuery.isBlank()) {
                activeStaff
            } else {
                activeStaff.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.role.contains(searchQuery, ignoreCase = true)
                }
            }
            return searched.sortedByDescending { it.employerFlatIds.contains(currentFlatId) }
        }
}