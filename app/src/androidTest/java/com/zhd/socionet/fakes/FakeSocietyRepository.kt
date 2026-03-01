package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.data.repository.SocietyRepository

class FakeSocietyRepository : SocietyRepository {
    override suspend fun getSocietyResidents(societyId: String): Result<List<UserProfile>> = Result.success(listOf(
        UserProfile(
            id = "r1", 
            fullName = "Resident One", 
            roles = listOf(UserRole.OWNER), 
            societyId = societyId,
            wingId = "A",
            flatId = "SOC1_A_101",
            flatNumber = "101"
        )
    ))
    override suspend fun removeUserFromSociety(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun addStaffMember(societyId: String, name: String, phone: String, role: UserRole): Result<Unit> = Result.success(Unit)
}
