package com.zhd.socionet.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val fcm = FirebaseMessaging.getInstance()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val snapshot = firestore.collection(AppConstants.COLL_USERS).document(userId)
                    .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
                    .get().await()

                if (snapshot.exists()) {
                    val prefs = snapshot.toObject(SettingsPreferences::class.java)
                    if (prefs != null) {
                        _uiState.update { it.copy(prefs = prefs) }
                        syncFcmTopics(prefs)
                    }
                } else {
                    saveSettings(_uiState.value.prefs)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load settings") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun saveSettings(newState: SettingsPreferences) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection(AppConstants.COLL_USERS).document(userId)
                    .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
                    .set(newState).await()
                _uiState.update { it.copy(prefs = newState) }
            } catch (e: Exception) {
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
        if (enabled && _uiState.value.prefs.notificationsEnabled) fcm.subscribeToTopic("visitors")
        else fcm.unsubscribeFromTopic("visitors")
    }

    fun toggleNoticeAlerts(enabled: Boolean) {
        val newState = _uiState.value.prefs.copy(notifyOnNotices = enabled)
        saveSettings(newState)
        if (enabled && _uiState.value.prefs.notificationsEnabled) fcm.subscribeToTopic("notices")
        else fcm.unsubscribeFromTopic("notices")
    }

    fun togglePaymentAlerts(enabled: Boolean) {
        val newState = _uiState.value.prefs.copy(notifyOnPayments = enabled)
        saveSettings(newState)
    }

    fun setTheme(mode: ThemeMode) {
        val newState = _uiState.value.prefs.copy(themeMode = mode)
        saveSettings(newState)
    }

    private fun syncFcmTopics(prefs: SettingsPreferences) {
        if (!prefs.notificationsEnabled) {
            fcm.unsubscribeFromTopic("all_alerts")
            fcm.unsubscribeFromTopic("notices")
            fcm.unsubscribeFromTopic("visitors")
        } else {
            fcm.subscribeToTopic("all_alerts")
            if (prefs.notifyOnNotices) fcm.subscribeToTopic("notices") else fcm.unsubscribeFromTopic("notices")
            if (prefs.notifyOnVisitors) fcm.subscribeToTopic("visitors") else fcm.unsubscribeFromTopic("visitors")
        }
    }
}

data class SettingsUiState(
    val prefs: SettingsPreferences = SettingsPreferences(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)