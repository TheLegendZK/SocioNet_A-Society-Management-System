package com.zhd.socionet.fakes

import android.net.Uri
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.repository.ProfileRepository

class FakeProfileRepository : ProfileRepository {
    override suspend fun getUserProfile(userId: String): Result<UserProfile> = 
        Result.success(UserProfile(
            id = userId, 
            fullName = "Admin User", 
            email = "admin@socionet.com",
            phoneNumber = "1234567890",
            assignedFlatId = "SOC1_A_101"
        ))
    
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.success(Unit)
    override suspend fun updatePassword(currentPass: String, newPass: String): Result<Unit> = Result.success(Unit)
    override suspend fun logout() {}
    override suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> = Result.success("")
}
