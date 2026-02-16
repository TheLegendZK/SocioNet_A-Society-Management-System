package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SocietyRepository {
    suspend fun getSocietyResidents(societyId: String): Result<List<UserProfile>>
}

@Singleton
class SocietyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SocietyRepository {

    override suspend fun getSocietyResidents(societyId: String): Result<List<UserProfile>> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_USERS)
                .whereGreaterThanOrEqualTo("assignedFlatId", societyId)
                .whereLessThanOrEqualTo("assignedFlatId", societyId + "\uf8ff")
                .get()
                .await()

            val users = snapshot.toObjects(UserProfile::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}