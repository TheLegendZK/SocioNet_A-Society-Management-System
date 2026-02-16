package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun triggerSos(userId: String, flatId: String, type: String): Result<Unit> {
        return try {
            val alert = hashMapOf(
                "type" to "SOS",
                "subType" to type,
                "raisedByUserId" to userId,
                "flatId" to flatId,
                "timestamp" to Timestamp.now(),
                "status" to "ACTIVE",
                "message" to "EMERGENCY ALERT FROM $flatId"
            )

            firestore.collection(AppConstants.COLL_EMERGENCY_ALERTS).add(alert).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}