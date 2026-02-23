package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository : AuthRepository {
    override fun getCurrentUserId(): String? = "dummy_user_id"
    override fun observeAuthState(): Flow<String?> = flowOf("dummy_user_id")
    override suspend fun loginUser(email: String, pass: String): Result<UserProfile> = Result.failure(Exception("Not implemented"))
    override suspend fun registerUser(email: String, password: String, profile: UserProfile): Result<String> = Result.success("123")
    override suspend fun fetchSocieties(): List<Pair<String, String>> = listOf("soc1" to "Gokuldham Society")
    override suspend fun fetchWings(societyId: String): List<String> = listOf("A", "B")
    override suspend fun fetchFlats(societyId: String, wing: String): List<Pair<String, String>> = listOf("f1" to "101", "f2" to "102")
    override suspend fun checkFlatOccupancy(flatId: String): String? = null
    override suspend fun updateFcmToken(token: String) {}
    override suspend fun refreshFcmToken() {}
    override suspend fun getUserProfile(userId: String): UserProfile? = null
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
}
