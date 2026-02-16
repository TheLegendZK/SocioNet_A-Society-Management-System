package com.zhd.socionet.ui.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.SocietyRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocietyDirectoryUiState())
    val uiState: StateFlow<SocietyDirectoryUiState> = _uiState.asStateFlow()

    fun loadDirectory(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getSocietyResidents(societyId)

            result.onSuccess { users ->
                val sortedUsers = users.sortedBy { it.assignedFlatId }
                _uiState.update { it.copy(isLoading = false, residents = sortedUsers) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

data class SocietyDirectoryUiState(
    val isLoading: Boolean = false,
    val residents: List<UserProfile> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
) {
    val filteredResidents: List<UserProfile>
        get() = if (searchQuery.isBlank()) {
            residents
        } else {
            residents.filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                        (it.assignedFlatId ?: "").contains(searchQuery, ignoreCase = true)
            }
        }
}