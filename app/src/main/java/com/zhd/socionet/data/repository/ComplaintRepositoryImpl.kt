package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zhd.socionet.data.model.Complaint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ComplaintRepository {
    suspend fun fileComplaint(complaint: Complaint): Result<Unit>
    suspend fun getComplaintsForFlat(flatId: String): List<Complaint>
    suspend fun getAllComplaints(): List<Complaint> // <--- NEW FOR ADMIN
    suspend fun updateStatus(complaintId: String, newStatus: String): Result<Unit>
}

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ComplaintRepository {

    override suspend fun fileComplaint(complaint: Complaint): Result<Unit> {
        return try {
            val docRef = firestore.collection("complaints").document()
            val finalComplaint = complaint.copy(id = docRef.id, createdAt = Timestamp.now())
            docRef.set(finalComplaint).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getComplaintsForFlat(flatId: String): List<Complaint> {
        return try {
            val snapshot = firestore.collection("complaints")
                .whereEqualTo("flatId", flatId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Complaint::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- NEW: Admin View ---
    override suspend fun getAllComplaints(): List<Complaint> {
        return try {
            // Fetches ALL complaints sorted by date (newest first)
            val snapshot = firestore.collection("complaints")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Complaint::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateStatus(complaintId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection("complaints").document(complaintId)
                .update("status", newStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}