package com.zhd.socionet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.remote.FirestoreHelper
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
    suspend fun updateFcmToken(token: String)
    suspend fun refreshFcmToken()
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirestoreHelper,
    private val crashlytics: FirebaseCrashlytics
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
            val doc = db.getUsersRef().document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)

            if (profile == null) {
                Result.failure(Exception("User profile not found in database"))
            } else {
                // Refresh token on successful login
                refreshFcmToken()
                Result.success(profile)
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
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
            db.getUsersRef().document(userId).set(finalProfile).await()
            
            // Refresh token on successful registration
            refreshFcmToken()

            Result.success(userId)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun fetchSocieties(): List<Pair<String, String>> {
        return try {
            val snapshot = db.getSocietiesRef().get().await()
            snapshot.documents.map { it.id to (it.getString("name") ?: "Unknown") }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            throw e
        }
    }

    override suspend fun fetchWings(societyId: String): List<String> {
        if (societyId.isBlank()) return emptyList()
        return try {
            val doc =
                db.getSocietiesRef().document(societyId).get().await()
            val wingsList = doc.get("wings") as? List<*>
            wingsList?.filterIsInstance<String>() ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            throw e
        }
    }

    override suspend fun fetchFlats(societyId: String, wing: String): List<Pair<String, String>> {
        return try {
            val snapshot = db.getFlatsRef()
                .whereEqualTo("societyId", societyId)
                .whereEqualTo("wing", wing)
                .get().await()

            snapshot.documents.map { it.id to (it.getString("flatNumber") ?: "0") }
                .sortedWith(compareBy<Pair<String, String>> { pair ->
                    pair.second.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
                }.thenBy { pair ->
                    pair.second.filter { it.isLetter() }
                })
        } catch (e: Exception) {
            crashlytics.recordException(e)
            throw e
        }
    }

    override suspend fun checkFlatOccupancy(flatId: String): String? {
        if (flatId.isBlank()) return null
        return try {
            val doc = db.getFlatsRef().document(flatId).get().await()
            doc.getString("primaryOwnerId")
        } catch (e: Exception) {
            crashlytics.recordException(e)
            throw e
        }
    }

    override suspend fun updateFcmToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            db.getUsersRef().document(userId)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }
    }

    override suspend fun refreshFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            updateFcmToken(token)
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = db.getUsersRef().document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            null
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}