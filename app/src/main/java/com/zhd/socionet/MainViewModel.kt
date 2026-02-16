package com.zhd.socionet

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(ThemeMode.SYSTEM)
    val currentTheme: StateFlow<ThemeMode> = _currentTheme.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToSettings(user.uid)
            }
        }
    }

    private fun listenToSettings(userId: String) {
        firestore.collection(AppConstants.COLL_USERS).document(userId)
            .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val prefs = snapshot.toObject(SettingsPreferences::class.java)
                    _currentTheme.update { prefs?.themeMode ?: ThemeMode.SYSTEM }
                }
            }
    }
}