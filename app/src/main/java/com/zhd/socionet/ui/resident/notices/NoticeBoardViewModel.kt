package com.zhd.socionet.ui.resident.notices

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeBoardViewModel @Inject constructor(
    private val repository: NoticeRepository
) : ViewModel() {

    var uiState by mutableStateOf(NoticeBoardUiState())
        private set

    fun loadNotices(userWing: String, userRoles: List<UserRole>) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // Determine if user is an owner
            val isOwner = userRoles.contains(UserRole.OWNER)

            val notices = repository.getNoticesForUser(userWing, isOwner)
            uiState = uiState.copy(isLoading = false, notices = notices)
        }
    }
}

data class NoticeBoardUiState(
    val isLoading: Boolean = false,
    val notices: List<Notice> = emptyList()
)