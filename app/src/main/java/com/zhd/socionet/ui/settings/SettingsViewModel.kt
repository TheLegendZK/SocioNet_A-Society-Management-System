package com.zhd.socionet.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var uiState by mutableStateOf(SettingsPreferences())
        private set

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val fcm = FirebaseMessaging.getInstance()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("config").document("settings")
                    .get().await()

                if (snapshot.exists()) {
                    val prefs = snapshot.toObject(SettingsPreferences::class.java)
                    if (prefs != null) {
                        uiState = prefs
                        // Sync FCM topics with loaded prefs (in case of new install)
                        syncFcmTopics(prefs)
                    }
                } else {
                    // First time? Create default
                    saveSettings(uiState)
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load settings"
            } finally {
                isLoading = false
            }
        }
    }

    private fun saveSettings(newState: SettingsPreferences) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("config").document("settings")
                    .set(newState).await()
                // Update local state only after success
                uiState = newState
            } catch (e: Exception) {
                errorMessage = "Failed to save changes"
            }
        }
    }

    // --- ACTIONS ---

    fun toggleNotifications(enabled: Boolean) {
        val newState = uiState.copy(notificationsEnabled = enabled)
        saveSettings(newState)
        syncFcmTopics(newState)
    }

    fun toggleVisitorAlerts(enabled: Boolean) {
        val newState = uiState.copy(notifyOnVisitors = enabled)
        saveSettings(newState)
        if (enabled && uiState.notificationsEnabled) fcm.subscribeToTopic("visitors")
        else fcm.unsubscribeFromTopic("visitors")
    }

    fun toggleNoticeAlerts(enabled: Boolean) {
        val newState = uiState.copy(notifyOnNotices = enabled)
        saveSettings(newState)
        if (enabled && uiState.notificationsEnabled) fcm.subscribeToTopic("notices")
        else fcm.unsubscribeFromTopic("notices")
    }

    fun togglePaymentAlerts(enabled: Boolean) {
        val newState = uiState.copy(notifyOnPayments = enabled)
        saveSettings(newState)
        // Payment alerts are usually targeted by User ID, handled by backend
    }

    fun setTheme(mode: ThemeMode) {
        val newState = uiState.copy(themeMode = mode)
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