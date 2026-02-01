package com.zhd.socionet.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun checkAuthStatus(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            // Artificial delay to show logo (optional)
            delay(1500)

            if (currentUser == null) {
                onResult(null) // Not logged in
            } else {
                try {
                    // Fetch Profile to determine Role
                    val doc = firestore.collection("users").document(currentUser.uid).get().await()
                    val profile = doc.toObject(UserProfile::class.java)

                    if (profile != null) {
                        onResult(determineDestination(profile))
                    } else {
                        onResult(null) // User exists in Auth but no Profile in DB
                    }
                } catch (e: Exception) {
                    onResult(null) // Error fetching profile
                }
            }
        }
    }

    private fun determineDestination(profile: UserProfile): String {
        return when {
            profile.roles.contains(UserRole.SUPER_ADMIN) -> "admin_dashboard"
            profile.roles.contains(UserRole.SECURITY_GUARD) -> "security_dashboard"
            else -> "resident_home/${profile.assignedFlatId ?: "0"}/${profile.id}"
        }
    }
}