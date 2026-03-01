package com.zhd.socionet

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SettingsRepository
import com.zhd.socionet.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(ThemeMode.SYSTEM)
    val currentTheme: StateFlow<ThemeMode> = _currentTheme.asStateFlow()

    private val _deepLinkRoute = MutableStateFlow<String?>(null)
    val deepLinkRoute: StateFlow<String?> = _deepLinkRoute.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collectLatest { userId ->
                if (userId != null) {
                    launch { authRepository.refreshFcmToken() }
                    listenToSettings(userId)
                }
            }
        }
    }

    private suspend fun listenToSettings(userId: String) {
        settingsRepository.getThemeMode(userId).collectLatest { theme ->
            _currentTheme.value = theme
        }
    }

    fun handleDeepLink(intent: Intent?) {
        val route = intent?.getStringExtra("navigation_route")
        if (!route.isNullOrBlank()) {
            _deepLinkRoute.value = route
        } else {
            // FALLBACK: When app is in background, Android OS handles the notification 
            // and puts FCM data directly into the intent extras.
            val hostFlatId = intent?.getStringExtra("hostFlatId")
            val targetUserId = intent?.getStringExtra("targetUserId")
            val entryId = intent?.getStringExtra("entryId")

            // If we have flat and user info, navigate to the Approvals tab (tab=1)
            if (!hostFlatId.isNullOrBlank() && !targetUserId.isNullOrBlank()) {
                _deepLinkRoute.value = Routes.buildResidentCreatePass(hostFlatId, targetUserId, tab = 1)
            } else if (!entryId.isNullOrBlank()) {
                // We might only have entryId in some payloads, try to find user info or navigate to a general list
                // For now, focusing on the Visitor Approval case which has both IDs.
            }
        }
    }

    fun onDeepLinkHandled() {
        _deepLinkRoute.value = null
    }
}
