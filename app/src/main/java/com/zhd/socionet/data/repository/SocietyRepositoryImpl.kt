package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
            // We find users belonging to this society.
            // Since flatId is formatted as "societyId_Wing_FlatNo", we can use a range query
            // or simple string matching if the dataset was small.
            // For a robust query, we rely on the fact that verified users have an assignedFlatId.

            // NOTE: In a production app with millions of users, we would store 'societyId' as a
            // separate field on the UserProfile for indexed querying.
            // For this implementation, we will fetch users who have a flat assigned containing the societyId.

            val snapshot = firestore.collection("users")
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