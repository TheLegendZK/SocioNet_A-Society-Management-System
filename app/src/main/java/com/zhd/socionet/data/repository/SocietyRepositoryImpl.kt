package com.zhd.socionet.data.repository

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface SocietyRepository {
    suspend fun getSocietyResidents(societyId: String): Result<List<UserProfile>>
    suspend fun removeUserFromSociety(userId: String): Result<Unit>
    suspend fun addStaffMember(societyId: String, name: String, phone: String, role: UserRole): Result<Unit>
}

@Singleton
class SocietyRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper
) : SocietyRepository {

    override suspend fun getSocietyResidents(societyId: String): Result<List<UserProfile>> {
        return try {
            val snapshot = db.getUsersRef()
                .whereEqualTo("societyId", societyId)
                .get()
                .await()

            val users = snapshot.toObjects(UserProfile::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeUserFromSociety(userId: String): Result<Unit> {
        return try {
            db.getUsersRef().document(userId)
                .update(
                    mapOf(
                        "societyId" to null,
                        "wingId" to null,
                        "flatId" to null,
                        "societyName" to null,
                        "flatNumber" to null,
                        "isVerified" to false
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addStaffMember(societyId: String, name: String, phone: String, role: UserRole): Result<Unit> {
        return try {
            val staffId = UUID.randomUUID().toString()
            val newProfile = UserProfile(
                id = staffId,
                fullName = name,
                phoneNumber = phone,
                roles = listOf(role),
                societyId = societyId,
                isVerified = true
            )

            db.getUsersRef().document(staffId).set(newProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}