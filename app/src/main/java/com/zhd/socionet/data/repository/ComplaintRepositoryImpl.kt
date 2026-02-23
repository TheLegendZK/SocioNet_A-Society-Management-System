package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.local.ComplaintDao
import com.zhd.socionet.data.local.ComplaintEntity
import com.zhd.socionet.data.model.Complaint
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.data.model.ComplaintScope
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
    private val firestore: FirebaseFirestore,
    private val dao: ComplaintDao,
    private val crashlytics: FirebaseCrashlytics
) : ComplaintRepository {

    override suspend fun fileComplaint(complaint: Complaint): Result<Unit> {
        return try {
            val docRef = firestore.collection(AppConstants.COLL_COMPLAINTS).document()
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
            val snapshot = firestore.collection(AppConstants.COLL_COMPLAINTS)
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
            val snapshot = firestore.collection(AppConstants.COLL_COMPLAINTS)
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
            firestore.collection(AppConstants.COLL_COMPLAINTS).document(complaintId)
                .update("status", newStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}

fun Complaint.toEntity() = ComplaintEntity(id, title, description, category.name, status, flatId ?: "", createdAt.toDate().time)
fun ComplaintEntity.toDomain() = Complaint(id, title, description, null, ComplaintCategory.valueOf(category), ComplaintScope.PERSONAL, "", flatId, status, Timestamp(
    Date(createdAt)
))