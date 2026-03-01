package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.AdminUserRepository

class FakeAdminUserRepository : AdminUserRepository {
    override suspend fun getPendingUsers(societyId: String): List<UserProfile> = listOf(
        UserProfile(
            id = "pending_1",
            fullName = "John Doe",
            roles = listOf(UserRole.OWNER),
            societyId = societyId,
            wingId = "A",
            flatId = "SOC1_A_101",
            flatNumber = "101"
        )
    )
    override suspend fun approveUser(user: UserProfile): Result<Unit> = Result.success(Unit)
    override suspend fun rejectUser(userId: String): Result<Unit> = Result.success(Unit)
}
