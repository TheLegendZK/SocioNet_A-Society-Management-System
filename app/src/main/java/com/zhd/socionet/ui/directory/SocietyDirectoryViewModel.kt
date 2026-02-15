package com.zhd.socionet.ui.directory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.SocietyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocietyDirectoryViewModel @Inject constructor(
    private val repository: SocietyRepository,
) : ViewModel() {

    var uiState by mutableStateOf(SocietyDirectoryUiState())
        private set

    fun loadDirectory(societyId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = repository.getSocietyResidents(societyId)

            result.onSuccess { users ->
                // Sort by Flat ID (e.g. A_101, A_102)
                val sortedUsers = users.sortedBy { it.assignedFlatId }
                uiState = uiState.copy(isLoading = false, residents = sortedUsers)
            }.onFailure {
                uiState = uiState.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
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