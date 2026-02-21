package com.zhd.socionet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.ThemeMode
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SettingsRepository
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

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collectLatest { userId ->
                if (userId != null) {
                    launch { authRepository.refreshFcmToken() }
                    listenToSettings(userId)
                }
                // If userId is null (logged out), we could reset theme to System,
                // but keeping last known theme is also fine UX.
            }
        }
    }

    private suspend fun listenToSettings(userId: String) {
        settingsRepository.getThemeMode(userId).collectLatest { theme ->
            _currentTheme.value = theme
        }
    }
}
