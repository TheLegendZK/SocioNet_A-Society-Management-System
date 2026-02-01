package com.zhd.socionet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var currentTheme by mutableStateOf(ThemeMode.SYSTEM)
        private set

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToSettings(user.uid)
            }
        }
    }

    private fun listenToSettings(userId: String) {
        firestore.collection("users").document(userId)
            .collection("config").document("settings")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val prefs = snapshot.toObject(SettingsPreferences::class.java)
                    currentTheme = prefs?.themeMode ?: ThemeMode.SYSTEM
                }
            }
    }
}