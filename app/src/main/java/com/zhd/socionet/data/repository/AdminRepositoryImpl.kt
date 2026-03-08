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
    suspend fun updateSociety(society: Society): Result<Unit>
    suspend fun deleteSociety(societyId: String): Result<Unit>
    suspend fun getAllSocieties(): Result<List<Society>>
    suspend fun batchCreateFlats(flats: List<Flat>): Result<Unit>
    suspend fun deleteFlatsForSociety(societyId: String): Result<Unit>
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

    override suspend fun updateSociety(society: Society): Result<Unit> {
        return try {
            firestore.collection(AppConstants.COLL_SOCIETIES).document(society.id).set(society).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSociety(societyId: String): Result<Unit> {
        return try {
            firestore.collection(AppConstants.COLL_SOCIETIES).document(societyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllSocieties(): Result<List<Society>> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_SOCIETIES).get().await()
            val societies = snapshot.toObjects(Society::class.java)
            Result.success(societies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun batchCreateFlats(flats: List<Flat>): Result<Unit> {
        return try {
            val batch = firestore.batch()

            flats.forEach { flat ->
                if (flat.id.isNotBlank()) {
                    val docRef = firestore.collection(AppConstants.COLL_FLATS).document(flat.id)
                    batch.set(docRef, flat)
                }
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFlatsForSociety(societyId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_FLATS)
                .whereEqualTo("societyId", societyId)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}