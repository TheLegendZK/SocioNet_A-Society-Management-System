package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.repository.StaffRepository

class FakeStaffRepository : StaffRepository {
    override suspend fun addStaff(staff: ServiceStaff): Result<Unit> = Result.success(Unit)
    override suspend fun getStaffList(societyId: String): List<ServiceStaff> = listOf(
        ServiceStaff(id = "s1", name = "Staff One", role = "Maid", societyId = "SOC1")
    )
    override suspend fun updateStaffStatus(staffId: String, isActive: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun linkStaffToFlat(staffId: String, flatId: String): Result<Unit> = Result.success(Unit)
    override suspend fun unlinkStaffFromFlat(staffId: String, flatId: String): Result<Unit> = Result.success(Unit)
}
