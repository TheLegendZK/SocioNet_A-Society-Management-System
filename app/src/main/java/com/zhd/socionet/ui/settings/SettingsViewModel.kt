package com.zhd.socionet.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = settingsRepository.getSettings(userId)

            result.onSuccess { prefs ->
                _uiState.update { it.copy(isLoading = false, prefs = prefs) }
                syncFcmTopics(prefs)
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load settings") }
            }
        }
    }

    private fun saveSettings(newState: SettingsPreferences) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = settingsRepository.updateSettings(userId, newState)
            if (result.isSuccess) {
                _uiState.update { it.copy(prefs = newState) }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to save changes") }
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val newState = _uiState.value.prefs.copy(notificationsEnabled = enabled)
        saveSettings(newState)
        syncFcmTopics(newState)
    }

    fun toggleVisitorAlerts(enabled: Boolean) {
        val newState = _uiState.value.prefs.copy(notifyOnVisitors = enabled)
        saveSettings(newState)
        if (enabled && _uiState.value.prefs.notificationsEnabled) {
            settingsRepository.setNotificationTopic("visitors", true)
        } else {
            settingsRepository.setNotificationTopic("visitors", false)
        }
    }

    fun toggleNoticeAlerts(enabled: Boolean) {
        val newState = _uiState.value.prefs.copy(notifyOnNotices = enabled)
        saveSettings(newState)
        if (enabled && _uiState.value.prefs.notificationsEnabled) {
            settingsRepository.setNotificationTopic("notices", true)
        } else {
            settingsRepository.setNotificationTopic("notices", false)
        }
    }

    /*fun togglePaymentAlerts(enabled: Boolean) {
        val newState = _uiState.value.prefs.copy(notifyOnPayments = enabled)
        saveSettings(newState)
    }*/

    fun setTheme(mode: ThemeMode) {
        val newState = _uiState.value.prefs.copy(themeMode = mode)
        saveSettings(newState)
    }

    private fun syncFcmTopics(prefs: SettingsPreferences) {
        if (!prefs.notificationsEnabled) {
            settingsRepository.setNotificationTopic("all_alerts", false)
            settingsRepository.setNotificationTopic("notices", false)
            settingsRepository.setNotificationTopic("visitors", false)
        } else {
            settingsRepository.setNotificationTopic("all_alerts", true)
            settingsRepository.setNotificationTopic("notices", prefs.notifyOnNotices)
            settingsRepository.setNotificationTopic("visitors", prefs.notifyOnVisitors)
        }
    }
}

data class SettingsUiState(
    val prefs: SettingsPreferences = SettingsPreferences(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)