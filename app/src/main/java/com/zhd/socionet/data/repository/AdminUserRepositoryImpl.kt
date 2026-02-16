package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
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
        val snapshot = firestore.collection(AppConstants.COLL_USERS)
            .whereEqualTo("verified", false)
            .get().await()

        return snapshot.toObjects(UserProfile::class.java)
    }

    override suspend fun approveUser(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID is required"))
        return try {
            firestore.collection(AppConstants.COLL_USERS).document(userId)
                .update("verified", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectUser(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID is required"))
        return try {
            firestore.collection(AppConstants.COLL_USERS).document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}