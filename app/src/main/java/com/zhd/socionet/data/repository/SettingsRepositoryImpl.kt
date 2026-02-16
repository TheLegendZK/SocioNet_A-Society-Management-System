package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.SettingsPreferences
import com.zhd.socionet.data.model.ThemeMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.getValue

// FIX: Created a dedicated Repository for Settings to adhere to Clean Architecture (MVVM).
// ViewModels should not access Firestore directly.

interface SettingsRepository {
    fun getThemeMode(userId: String): Flow<ThemeMode>
    suspend fun getSettings(userId: String): Result<SettingsPreferences>
    suspend fun updateSettings(userId: String, settings: SettingsPreferences): Result<Unit>
    fun setNotificationTopic(topic: String, subscribe: Boolean)
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SettingsRepository {

    private val fcm by lazy { FirebaseMessaging.getInstance() }

    override fun getThemeMode(userId: String): Flow<ThemeMode> = callbackFlow {
        val listener = firestore.collection(AppConstants.COLL_USERS).document(userId)
            .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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

    override suspend fun getSettings(userId: String): Result<SettingsPreferences> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_USERS).document(userId)
                .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
                .get().await()

            if (snapshot.exists()) {
                val prefs = snapshot.toObject(SettingsPreferences::class.java)
                Result.success(prefs ?: SettingsPreferences())
            } else {
                // If no settings exist, create default ones
                val defaultPrefs = SettingsPreferences()
                updateSettings(userId, defaultPrefs)
                Result.success(defaultPrefs)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSettings(userId: String, settings: SettingsPreferences): Result<Unit> {
        return try {
            firestore.collection(AppConstants.COLL_USERS).document(userId)
                .collection(AppConstants.COLL_CONFIG).document(AppConstants.DOC_SETTINGS)
                .set(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun setNotificationTopic(topic: String, subscribe: Boolean) {
        try {
            if (subscribe) {
                fcm.subscribeToTopic(topic)
            } else {
                fcm.unsubscribeFromTopic(topic)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}