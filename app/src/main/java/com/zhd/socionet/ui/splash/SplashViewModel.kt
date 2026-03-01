package com.zhd.socionet.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    fun checkAuthStatus(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            // Delay to show branding
            delay(1500)

            val currentUserId = authRepository.getCurrentUserId()

            if (currentUserId == null) {
                onResult(null) // Not logged in
            } else {
                val result = profileRepository.getUserProfile(currentUserId)
                result.onSuccess { profile ->
                    onResult(determineDestination(profile))
                }.onFailure {
                    onResult(null) // Logged in but profile missing or error
                }
            }
        }
    }

    private fun determineDestination(profile: UserProfile): String {
        return when {
            profile.roles.contains(UserRole.SUPER_ADMIN) -> "admin_dashboard"
            profile.roles.contains(UserRole.SECURITY_GUARD) -> "security_dashboard"
            else -> "resident_home/${profile.flatId ?: profile.societyId ?: "0"}/${profile.id}"
        }
    }
}