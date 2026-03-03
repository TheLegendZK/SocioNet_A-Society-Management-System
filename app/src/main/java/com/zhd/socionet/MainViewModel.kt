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
            // Check raw FCM data if route is not present (background notification click)
            val hostFlatId = intent?.getStringExtra("hostFlatId")
            val targetUserId = intent?.getStringExtra("targetUserId")

            if (!hostFlatId.isNullOrBlank() && !targetUserId.isNullOrBlank()) {
                _deepLinkRoute.value = Routes.buildResidentCreatePass(hostFlatId, targetUserId, tab = 1)
            }
        }
    }

    fun onDeepLinkHandled() {
        _deepLinkRoute.value = null
    }

    // Helper to check if a deep link is currently being processed to prevent navigation override
    fun hasPendingDeepLink(): Boolean = _deepLinkRoute.value != null
}
