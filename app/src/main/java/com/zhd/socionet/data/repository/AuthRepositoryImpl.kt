package com.zhd.socionet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun loginUser(email: String, pass: String): Result<UserProfile> {
        return try {
            // 1. Auth
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed: No UID returned")

            // 2. Fetch Profile
            val doc = firestore.collection(AppConstants.COLL_USERS).document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)

            if (profile == null) {
                Result.failure(Exception("User profile not found in database"))
            } else {
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        if (societyId.isBlank()) return emptyList()
        val doc =
            firestore.collection(AppConstants.COLL_SOCIETIES).document(societyId).get().await()
        val wingsList = doc.get("wings") as? List<*>
        return wingsList?.filterIsInstance<String>() ?: emptyList()
    }

    override suspend fun fetchFlats(societyId: String, wing: String): List<Pair<String, String>> {
        val snapshot = firestore.collection(AppConstants.COLL_FLATS)
            .whereEqualTo("societyId", societyId)
            .whereEqualTo("wing", wing)
            .get().await()

        return snapshot.documents.map { it.id to (it.getString("flatNumber") ?: "0") }
            .sortedWith(compareBy<Pair<String, String>> { pair ->
                pair.second.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
            }.thenBy { pair ->
                pair.second.filter { it.isLetter() }
            })
    }

    override suspend fun checkFlatOccupancy(flatId: String): String? {
        if (flatId.isBlank()) return null
        val doc = firestore.collection(AppConstants.COLL_FLATS).document(flatId).get().await()
        return doc.getString("primaryOwnerId")
    }
}
