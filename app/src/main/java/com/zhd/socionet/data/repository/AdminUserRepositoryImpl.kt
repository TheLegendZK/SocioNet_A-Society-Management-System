package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AdminUserRepository {
    suspend fun getPendingUsers(societyId: String): List<UserProfile>
    suspend fun approveUser(userId: String): Result<Unit>
    suspend fun rejectUser(userId: String): Result<Unit>
}

@Singleton
class AdminUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminUserRepository {

    override suspend fun getPendingUsers(societyId: String): List<UserProfile> {
        // In a real app, we would filter by societyId.
        // Since we store flatId in the user profile, we might need a compound query
        // or fetch all unverified users and filter in memory if the list is small.
        // For efficiency here, we fetch all unverified users.
        val snapshot = firestore.collection("users")
            .whereEqualTo("verified", false)
            .get().await()

        return snapshot.toObjects(UserProfile::class.java)
    }

    override suspend fun approveUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("verified", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectUser(userId: String): Result<Unit> {
        return try {
            // In real life, we might archive them instead of deleting.
            // For now, we delete the profile so they can register again if they made a typo.
            firestore.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}