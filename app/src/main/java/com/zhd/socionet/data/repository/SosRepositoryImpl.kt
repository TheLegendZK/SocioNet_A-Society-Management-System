package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EmergencyAlert
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SosRepository {
    suspend fun triggerSos(alert: EmergencyAlert): Result<Unit>
}

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper
) : SosRepository {
    override suspend fun triggerSos(alert: EmergencyAlert): Result<Unit> {
        return try {
            val docRef = db.getEmergencyAlertsRef().document()
            val finalAlert = alert.copy(id = docRef.id, timestamp = Timestamp.now())
            docRef.set(finalAlert).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}