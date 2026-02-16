package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AdminRepository {
    suspend fun createSociety(society: Society): Result<String>
    suspend fun batchCreateFlats(flats: List<Flat>): Result<Unit>
}

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override suspend fun createSociety(society: Society): Result<String> {
        return try {
            val societyId = society.id.ifBlank { firestore.collection(AppConstants.COLL_SOCIETIES).document().id }
            val finalSociety = society.copy(id = societyId)

            firestore.collection(AppConstants.COLL_SOCIETIES).document(societyId).set(finalSociety).await()
            Result.success(societyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun batchCreateFlats(flats: List<Flat>): Result<Unit> {
        return try {
            val batch = firestore.batch()

            flats.forEach { flat ->
                val docRef = firestore.collection(AppConstants.COLL_FLATS).document(flat.id)
                batch.set(docRef, flat)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}