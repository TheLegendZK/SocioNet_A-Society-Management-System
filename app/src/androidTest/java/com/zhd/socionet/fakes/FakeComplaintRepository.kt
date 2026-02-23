package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.data.repository.ComplaintRepository

class FakeComplaintRepository : ComplaintRepository {
    override suspend fun fileComplaint(complaint: Complaint): Result<Unit> = Result.success(Unit)
    override suspend fun getComplaintsForFlat(flatId: String): List<Complaint> = emptyList()
    override suspend fun getAllComplaints(): List<Complaint> = listOf(
        Complaint(
            id = "c1",
            title = "Leaking Pipe",
            description = "Water is leaking in the kitchen.",
            category = ComplaintCategory.PLUMBING,
            status = "OPEN",
            flatId = "SOC1_A_101"
        )
    )
    override suspend fun updateStatus(complaintId: String, newStatus: String): Result<Unit> = Result.success(Unit)
}
