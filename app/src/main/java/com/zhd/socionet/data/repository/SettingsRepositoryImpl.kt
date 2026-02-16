package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

// FIX: Created a dedicated Repository for Settings to adhere to Clean Architecture (MVVM).
// ViewModels should not access Firestore directly.

interface SettingsRepository {
    fun getThemeMode(userId: String): Flow<ThemeMode>
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SettingsRepository {

    override fun getThemeMode(userId: String): Flow<ThemeMode> = callbackFlow {
        val listener = firestore.collection(AppConstants.COLL_USERS).document(userId)
            .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close flow on error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val prefs = snapshot.toObject(SettingsPreferences::class.java)
                    trySend(prefs?.themeMode ?: ThemeMode.SYSTEM)
                } else {
                    trySend(ThemeMode.SYSTEM)
                }
            }

        awaitClose { listener.remove() }
    }
}