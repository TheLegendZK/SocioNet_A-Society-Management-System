package com.zhd.socionet.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun login(email: String, pass: String, onLoginSuccess: (UserProfile) -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                // 1. Auth
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = authResult.user?.uid ?: throw Exception("Login failed")

                // 2. Fetch Profile to know Role
                val doc = firestore.collection("users").document(uid).get().await()
                val profile = doc.toObject(UserProfile::class.java)

                if (profile == null) {
                    throw Exception("User profile not found")
                } else {
                    onLoginSuccess(profile)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
