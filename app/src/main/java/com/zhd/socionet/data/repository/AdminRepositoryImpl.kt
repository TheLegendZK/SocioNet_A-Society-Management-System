package com.zhd.socionet.data.repository

import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import com.zhd.socionet.data.remote.FirestoreHelper
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
    private val db: FirestoreHelper
) : AdminRepository {

    override suspend fun createSociety(society: Society): Result<String> {
        return try {
            val societyId = society.id.ifBlank { db.getSocietiesRef().document().id }
            val finalSociety = society.copy(id = societyId)

            db.getSocietiesRef().document(societyId).set(finalSociety).await()
            Result.success(societyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSociety(society: Society): Result<Unit> {
        return try {
            db.getSocietiesRef().document(society.id).set(society).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSociety(societyId: String): Result<Unit> {
        return try {
            db.getSocietiesRef().document(societyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllSocieties(): Result<List<Society>> {
        return try {
            val snapshot = db.getSocietiesRef().get().await()
            val societies = snapshot.toObjects(Society::class.java)
            Result.success(societies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun batchCreateFlats(flats: List<Flat>): Result<Unit> {
        return try {
            val batch = db.getFirestore().batch()

            flats.forEach { flat ->
                if (flat.id.isNotBlank()) {
                    val docRef = db.getFlatsRef().document(flat.id)
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
            val snapshot = db.getFlatsRef()
                .whereEqualTo("societyId", societyId)
                .get()
                .await()

            val batch = db.getFirestore().batch()
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