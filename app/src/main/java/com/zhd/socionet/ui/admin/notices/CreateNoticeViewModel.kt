package com.zhd.socionet.ui.admin.notices

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.data.repository.AdminRepository
import com.zhd.socionet.data.repository.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val adminRepository: AdminRepository // To get list of Wings
) : ViewModel() {

    var uiState by mutableStateOf(CreateNoticeUiState())
        private set

    init {
        loadWings()
    }

    private fun loadWings() {
        // In a real app, we'd fetch the specific society ID from the logged-in admin's profile.
        // Hardcoded here for the prototype flow.
        val societyId = "WbZ6qVoyNlMi1Quch8o3" // Replace with dynamic ID
        viewModelScope.launch {
            // We reuse the AdminRepo to fetch wings for the dropdown
            // Assuming we added a getSociety function there, or we hardcode for now
            val wings = listOf("A", "B", "C") // simplified for this step
            uiState = uiState.copy(availableWings = wings)
        }
    }

    fun onFieldChange(title: String, content: String) {
        uiState = uiState.copy(title = title, content = content)
    }

    fun onPrioritySelected(priority: String) {
        uiState = uiState.copy(priority = priority)
    }

    fun onAudienceSelected(audience: String) {
        uiState = uiState.copy(targetAudience = audience)
    }

    fun postNotice(adminId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val notice = Notice(
                title = uiState.title,
                content = uiState.content,
                priority = uiState.priority,
                targetAudience = uiState.targetAudience,
                postedByAdminId = adminId
            )

            val result = noticeRepository.createNotice(notice)

            if (result.isSuccess) {
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } else {
                uiState = uiState.copy(isLoading = false, errorMessage = "Failed to post notice")
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