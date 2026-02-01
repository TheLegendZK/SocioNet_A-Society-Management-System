package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
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
                "subType" to type, // "Medical", "Fire", "Security"
                "raisedByUserId" to userId,
                "flatId" to flatId,
                "timestamp" to Timestamp.now(),
                "status" to "ACTIVE", // Guard sets to RESOLVED later
                "message" to "EMERGENCY ALERT FROM $flatId"
            )

            // Add to a special high-priority collection
            firestore.collection("emergency_alerts").add(alert).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}