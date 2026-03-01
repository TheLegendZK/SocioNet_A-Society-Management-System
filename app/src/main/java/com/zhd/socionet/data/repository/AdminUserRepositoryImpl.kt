package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AdminUserRepository {
    suspend fun getPendingUsers(societyId: String): List<UserProfile>
    suspend fun approveUser(user: UserProfile): Result<Unit>
    suspend fun rejectUser(userId: String): Result<Unit>
}

@Singleton
class AdminUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminUserRepository {

    override suspend fun getPendingUsers(societyId: String): List<UserProfile> {
        val snapshot = firestore.collection(AppConstants.COLL_USERS)
            .whereEqualTo("societyId", societyId)
            .whereEqualTo("verified", false)
            .get().await()

        return snapshot.toObjects(UserProfile::class.java)
    }

    override suspend fun approveUser(user: UserProfile): Result<Unit> {
        if (user.id.isBlank()) return Result.failure(Exception("User ID is required"))
        return try {
            val batch = firestore.batch()

            // 1. Mark User as Verified
            val userRef = firestore.collection(AppConstants.COLL_USERS).document(user.id)
            batch.update(userRef, "verified", true)

            // 2. Denormalization: Update Flat with Owner info if applicable
            if (user.roles.contains(UserRole.OWNER) && !user.flatId.isNullOrBlank()) {
                val flatRef = firestore.collection(AppConstants.COLL_FLATS).document(user.flatId)
                batch.update(flatRef, mapOf(
                    "primaryOwnerId" to user.id,
                    "primaryOwnerName" to user.fullName,
                    "isOccupied" to true
                ))
            } else if (user.roles.contains(UserRole.TENANT) && !user.flatId.isNullOrBlank()) {
                val flatRef = firestore.collection(AppConstants.COLL_FLATS).document(user.flatId)
                batch.update(flatRef, mapOf(
                    "tenantIds" to FieldValue.arrayUnion(user.id),
                    "isOccupied" to true
                ))
            }

            batch.commit().await()
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
