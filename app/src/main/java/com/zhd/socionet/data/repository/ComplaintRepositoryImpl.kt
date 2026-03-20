package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.Query
import com.zhd.socionet.data.local.ComplaintDao
import com.zhd.socionet.data.local.ComplaintEntity
import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.data.model.ComplaintScope
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface ComplaintRepository {
    suspend fun fileComplaint(complaint: Complaint): Result<Unit>
    suspend fun getComplaintsForFlat(flatId: String): List<Complaint>
    suspend fun getAllComplaints(): List<Complaint>
    suspend fun updateStatus(complaintId: String, newStatus: String): Result<Unit>
}

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper,
    private val dao: ComplaintDao,
    private val crashlytics: FirebaseCrashlytics
) : ComplaintRepository {

    override suspend fun fileComplaint(complaint: Complaint): Result<Unit> {
        return try {
            val docRef = db.getComplaintsRef().document()
            val finalComplaint = complaint.copy(id = docRef.id, createdAt = Timestamp.now())
            docRef.set(finalComplaint).await()
            dao.insertComplaints(listOf(finalComplaint.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getComplaintsForFlat(flatId: String): List<Complaint> {
        if (flatId.isBlank()) return emptyList()
        try {
            val snapshot = db.getComplaintsRef()
                .whereEqualTo("flatId", flatId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val networkList = snapshot.toObjects(Complaint::class.java)
            dao.insertComplaints(networkList.map { it.toEntity() })
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }
        return dao.getComplaintsForFlat(flatId).map { it.toDomain() }
    }

    override suspend fun getAllComplaints(): List<Complaint> {
        try {
            val snapshot = db.getComplaintsRef()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val list = snapshot.toObjects(Complaint::class.java)
            dao.insertComplaints(list.map { it.toEntity() })
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }

        return dao.getAllComplaints().map { it.toDomain() }
    }

    override suspend fun updateStatus(complaintId: String, newStatus: String): Result<Unit> {
        if (complaintId.isBlank()) return Result.failure(Exception("Complaint ID is required"))
        return try {
            db.getComplaintsRef().document(complaintId)
                .update("status", newStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}

fun Complaint.toEntity() = ComplaintEntity(
    id = id,
    title = title,
    description = description,
    photoUrl = photoUrl,
    category = category.name,
    scope = scope.name,
    status = status,
    raisedByUserId = raisedByUserId,
    raisedByUserName = raisedByUserName,
    flatId = flatId ?: "",
    flatNumber = flatNumber,
    createdAt = createdAt.toDate().time
)

fun ComplaintEntity.toDomain() = Complaint(
    id = id,
    title = title,
    description = description,
    photoUrl = photoUrl,
    category = try { ComplaintCategory.valueOf(category) } catch (e: Exception) { ComplaintCategory.OTHER },
    scope = try { ComplaintScope.valueOf(scope) } catch (e: Exception) { ComplaintScope.PERSONAL },
    raisedByUserId = raisedByUserId,
    raisedByUserName = raisedByUserName,
    flatId = flatId,
    flatNumber = flatNumber,
    status = status,
    createdAt = Timestamp(Date(createdAt))
)