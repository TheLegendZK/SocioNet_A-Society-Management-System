package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.EmergencyAlert
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SosRepository {
    suspend fun triggerSos(alert: EmergencyAlert): Result<Unit>
}

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SosRepository {
    override suspend fun triggerSos(alert: EmergencyAlert): Result<Unit> {
        return try {
            val docRef = firestore.collection(AppConstants.COLL_EMERGENCY_ALERTS).document()
            val finalAlert = alert.copy(id = docRef.id, timestamp = Timestamp.now())
            docRef.set(finalAlert).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
