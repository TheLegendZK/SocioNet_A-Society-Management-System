package com.zhd.socionet.ui.admin.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.repository.AdminRepository
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
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNoticeUiState())
    val uiState: StateFlow<CreateNoticeUiState> = _uiState.asStateFlow()

    fun loadWings(societyId: String) {
        viewModelScope.launch {
            val wings = listOf("A", "B", "C") 
            _uiState.update { it.copy(availableWings = wings) }
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

    fun postNotice(adminId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val notice = Notice(
                title = _uiState.value.title,
                content = _uiState.value.content,
                priority = _uiState.value.priority,
                targetAudience = _uiState.value.targetAudience,
                postedByAdminId = adminId
            )

            val result = noticeRepository.createNotice(notice)

            if (result.isSuccess) {
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
    val availableWings: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)