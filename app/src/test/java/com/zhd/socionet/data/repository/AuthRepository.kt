package com.zhd.socionet.data.repository

import com.zhd.socionet.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun observeAuthState(): Flow<String?>
    suspend fun loginUser(email: String, pass: String): Result<UserProfile>
    suspend fun registerUser(email: String, password: String, profile: UserProfile): Result<String>
    suspend fun fetchSocieties(): List<Pair<String, String>>
    suspend fun fetchWings(societyId: String): List<String>
    suspend fun fetchFlats(
        societyId: String,
        wing: String,
    ): List<Pair<String, String>>

    suspend fun checkFlatOccupancy(flatId: String): String?
    suspend fun updateFcmToken(token: String)
    suspend fun refreshFcmToken()
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}
