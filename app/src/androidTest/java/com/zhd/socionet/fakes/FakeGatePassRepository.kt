package com.zhd.socionet.fakes

import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.repository.GatePassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeGatePassRepository : GatePassRepository {
    override suspend fun createGatePass(entry: GateEntry): Result<String> = Result.success("pass_1")
    override fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>> = flowOf(listOf(
        GateEntry(id = "req_1", visitorName = "Guest User", visitorType = VisitorType.GUEST, status = EntryStatus.PENDING_APPROVAL)
    ))
    override suspend fun getActivePasses(flatId: String): List<GateEntry> = emptyList()
    override fun getPastPassesFlow(flatId: String): Flow<List<GateEntry>> = flowOf(listOf(
        GateEntry(id = "hist_1", visitorName = "Delivery Guy", visitorType = VisitorType.DELIVERY, status = EntryStatus.CHECKED_IN, entryTime = Timestamp.now())
    ))
    override suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit> = Result.success(Unit)
    override fun getStaffLogsFlow(staffIds: List<String>): Flow<List<GateEntry>> = flowOf(emptyList())
    override suspend fun logStaffEntry(staffId: String, staffName: String, action: String, societyId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getUserByFlat(flatId: String): UserProfile? = null
    override suspend fun getAllFlats(): Result<List<Flat>> = Result.success(emptyList())
    override fun getSecurityLogsFlow(societyId: String): Flow<List<GateEntry>> = flowOf(emptyList())
}
