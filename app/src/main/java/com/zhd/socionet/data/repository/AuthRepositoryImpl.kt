package com.zhd.socionet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun registerUser(email: String, password: String, profile: UserProfile): Result<String>
    suspend fun fetchSocieties(): List<Pair<String, String>>
    suspend fun fetchWings(societyId: String): List<String>
    suspend fun fetchFlats(
        societyId: String,
        wing: String,
    ): List<Pair<String, String>>
    suspend fun checkFlatOccupancy(flatId: String): String?
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override suspend fun registerUser(
        email: String,
        password: String,
        profile: UserProfile,
    ): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Auth failed")

            val finalProfile = profile.copy(id = userId)
            firestore.collection(AppConstants.COLL_USERS).document(userId).set(finalProfile).await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchSocieties(): List<Pair<String, String>> {
        val snapshot = firestore.collection(AppConstants.COLL_SOCIETIES).get().await()
        return snapshot.documents.map { it.id to (it.getString("name") ?: "Unknown") }
    }

    override suspend fun fetchWings(societyId: String): List<String> {
        val doc = firestore.collection(AppConstants.COLL_SOCIETIES).document(societyId).get().await()
        return (doc.get("wings") as? List<String>) ?: emptyList()
    }

    override suspend fun fetchFlats(societyId: String, wing: String): List<Pair<String, String>> {
        val snapshot = firestore.collection(AppConstants.COLL_FLATS)
            .whereEqualTo("societyId", societyId)
            .whereEqualTo("wing", wing)
            .get().await()

        return snapshot.documents.map { it.id to (it.getString("flatNumber") ?: "0") }
            .sortedBy { it.second.toIntOrNull() ?: 0 }
    }

    override suspend fun checkFlatOccupancy(flatId: String): String? {
        val doc = firestore.collection(AppConstants.COLL_FLATS).document(flatId).get().await()
        return doc.getString("primaryOwnerId")
    }
}
