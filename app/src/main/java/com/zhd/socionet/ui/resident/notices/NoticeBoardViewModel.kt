package com.zhd.socionet.ui.resident.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeBoardViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeBoardUiState())
    val uiState: StateFlow<NoticeBoardUiState> = _uiState.asStateFlow()

    private var lastLoadedWing: String? = null
    private var lastLoadedSociety: String? = null

    fun loadNotices(societyId: String, userWing: String, userRoles: List<UserRole>) {
        // Optimization: Avoid setting isLoading = true if data is already present for same wing/society
        if (_uiState.value.notices.isNotEmpty() && lastLoadedWing == userWing && lastLoadedSociety == societyId) return

        lastLoadedWing = userWing
        lastLoadedSociety = societyId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val isOwner = userRoles.contains(UserRole.OWNER)
            val notices = repository.getNoticesForUser(societyId, userWing, isOwner)
            _uiState.update { it.copy(isLoading = false, notices = notices) }
        }
    }
}

data class NoticeBoardUiState(
    val isLoading: Boolean = false,
    val notices: List<Notice> = emptyList()
)
