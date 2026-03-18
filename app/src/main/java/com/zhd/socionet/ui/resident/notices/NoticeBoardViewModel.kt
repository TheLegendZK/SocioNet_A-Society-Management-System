package com.zhd.socionet.ui.resident.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.NoticeRepository
import com.zhd.socionet.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeBoardViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeBoardUiState())
    val uiState: StateFlow<NoticeBoardUiState> = _uiState.asStateFlow()

    private var lastLoadedUserId: String? = null
    private var lastLoadedSociety: String? = null

    fun loadNotices(societyId: String, userId: String) {
        if (_uiState.value.notices.isNotEmpty() && lastLoadedUserId == userId && lastLoadedSociety == societyId) return

        lastLoadedUserId = userId
        lastLoadedSociety = societyId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Fetch user profile first to get wing and roles
            val profileResult = profileRepository.getUserProfile(userId)
            profileResult.onSuccess { profile ->
                val wing = profile.wingId ?: ""
                val isOwner = profile.roles.contains(UserRole.OWNER)
                
                val notices = noticeRepository.getNoticesForUser(societyId, wing, isOwner)
                _uiState.update { it.copy(isLoading = false, notices = notices) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // Keep the old method for backward compatibility or direct calls if needed, 
    // but we can also just migrate everything to the userId version.
    fun loadNotices(societyId: String, userWing: String, userRoles: List<UserRole>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isOwner = userRoles.contains(UserRole.OWNER)
            val notices = noticeRepository.getNoticesForUser(societyId, userWing, isOwner)
            _uiState.update { it.copy(isLoading = false, notices = notices) }
        }
    }
}

data class NoticeBoardUiState(
    val isLoading: Boolean = false,
    val notices: List<Notice> = emptyList(),
    val errorMessage: String? = null
)
