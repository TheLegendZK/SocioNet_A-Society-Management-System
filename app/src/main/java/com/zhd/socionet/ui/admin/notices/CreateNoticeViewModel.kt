package com.zhd.socionet.ui.admin.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNoticeUiState())
    val uiState: StateFlow<CreateNoticeUiState> = _uiState.asStateFlow()

    fun loadInitialData(adminId: String, providedSocietyId: String?) {
        viewModelScope.launch {
            val finalSocietyId = if (providedSocietyId.isNullOrBlank()) {
                val profile = authRepository.getUserProfile(adminId)
                profile?.societyId ?: ""
            } else {
                providedSocietyId
            }

            if (finalSocietyId.isNotBlank()) {
                _uiState.update { it.copy(resolvedSocietyId = finalSocietyId) }
                loadWings(finalSocietyId)
                loadNotices(finalSocietyId)
            }
        }
    }

    private fun loadWings(societyId: String) {
        viewModelScope.launch {
            val wings = authRepository.fetchWings(societyId)
            _uiState.update { it.copy(availableWings = wings) }
        }
    }

    fun loadNotices(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNotices = true) }
            noticeRepository.getNoticesForSociety(societyId).onSuccess { notices ->
                _uiState.update { it.copy(notices = notices, isLoadingNotices = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingNotices = false, errorMessage = e.message) }
            }
        }
    }

    fun onFieldChange(title: String, content: String) {
        _uiState.update { it.copy(title = title, content = content) }
    }

    fun onPrioritySelected(priority: String) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onAudienceSelected(audience: String) {
        _uiState.update { it.copy(targetAudience = audience) }
    }

    fun selectNoticeForEdit(notice: Notice) {
        _uiState.update { 
            it.copy(
                editingNoticeId = notice.id,
                title = notice.title,
                content = notice.content,
                priority = notice.priority,
                targetAudience = notice.targetAudience
            )
        }
    }

    fun resetForm() {
        _uiState.update { 
            it.copy(
                editingNoticeId = null,
                title = "",
                content = "",
                priority = "NORMAL",
                targetAudience = "ALL",
                isSuccess = false
            )
        }
    }

    fun deleteNotice(noticeId: String) {
        val societyId = _uiState.value.resolvedSocietyId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            noticeRepository.deleteNotice(noticeId).onSuccess {
                loadNotices(societyId)
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun postNotice(adminId: String) {
        val societyId = _uiState.value.resolvedSocietyId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val profile = authRepository.getUserProfile(adminId)
            val editingId = _uiState.value.editingNoticeId

            val notice = Notice(
                id = editingId ?: "",
                societyId = societyId,
                title = _uiState.value.title,
                content = _uiState.value.content,
                priority = _uiState.value.priority,
                targetAudience = _uiState.value.targetAudience,
                postedByAdminId = adminId,
                authorName = profile?.fullName
            )

            val result = if (editingId != null) {
                noticeRepository.updateNotice(notice)
            } else {
                noticeRepository.createNotice(notice)
            }

            if (result.isSuccess) {
                loadNotices(societyId)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to post notice") }
            }
        }
    }
}

data class CreateNoticeUiState(
    val title: String = "",
    val content: String = "",
    val priority: String = "NORMAL",
    val targetAudience: String = "ALL",
    val resolvedSocietyId: String = "",
    val availableWings: List<String> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val editingNoticeId: String? = null,
    val isLoading: Boolean = false,
    val isLoadingNotices: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
