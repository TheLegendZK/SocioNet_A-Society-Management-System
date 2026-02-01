package com.zhd.socionet.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.zhd.socionet.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun updatePassword(currentPass: String, newPass: String): Result<Unit>
    suspend fun logout()
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("Profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            // Update Firestore
            firestore.collection("users").document(profile.id).set(profile).await()

            // Also update Firebase Auth Profile (DisplayName/PhotoURL) for consistency
            val user = auth.currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(profile.fullName)
                // .setPhotoUri(Uri.parse(profile.profilePhotoUrl)) // If you implement photo upload
                .build()
            user?.updateProfile(profileUpdates)?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(currentPass: String, newPass: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            // Re-authenticate first
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}