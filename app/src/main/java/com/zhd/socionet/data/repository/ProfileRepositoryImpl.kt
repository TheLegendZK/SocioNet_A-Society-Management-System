package com.zhd.socionet.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun updatePassword(currentPass: String, newPass: String): Result<Unit>
    suspend fun logout()
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String>
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @get:ApplicationContext private val context: Context
) : ProfileRepository {

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_USERS).document(userId).get().await()
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
            firestore.collection(AppConstants.COLL_USERS).document(profile.id).set(profile).await()

            val user = auth.currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(profile.fullName)
                .setPhotoUri(profile.profilePhotoUrl.toUri())
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

            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return try {
            // 1. Get ContentResolver and MimeType
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg" // Fallback to jpeg

            // 2. Get Extension
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            // 3. FIX: Create Metadata (Required for rules checking contentType)
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()

            // 4. FIX: Use Subfolder Path to match Rules
            // Old Path:  .../profile_photos/userId.jpg  (Mismatch!)
            // New Path:  .../profile_photos/userId/profile.png (Matches: match /{userId}/{fileName})
            val ref = storage.reference.child("${AppConstants.PATH_PROFILE_PICS}$userId/profile.$extension")

            // 5. Upload with Metadata
            ref.putFile(imageUri, metadata).await()

            val downloadUrl = ref.downloadUrl.await().toString()

            // Update Firestore
            firestore.collection(AppConstants.COLL_USERS).document(userId)
                .update("profilePhotoUrl", downloadUrl).await()

            // Update Firebase Auth Profile
            val user = auth.currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUrl.toUri())
                .build()
            user?.updateProfile(profileUpdates)?.await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}