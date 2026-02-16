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

    fun loadNotices(userWing: String, userRoles: List<UserRole>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Determine if user is an owner
            val isOwner = userRoles.contains(UserRole.OWNER)

            val notices = repository.getNoticesForUser(userWing, isOwner)
            _uiState.update { it.copy(isLoading = false, notices = notices) }
        }
    }
}

data class NoticeBoardUiState(
    val isLoading: Boolean = false,
    val notices: List<Notice> = emptyList()
)
