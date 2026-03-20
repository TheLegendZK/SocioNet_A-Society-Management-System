package com.zhd.socionet.data.repository

import com.google.firebase.firestore.FieldValue
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface StaffRepository {
    suspend fun addStaff(staff: ServiceStaff): Result<Unit>
    suspend fun getStaffList(societyId: String): List<ServiceStaff>
    suspend fun updateStaffStatus(staffId: String, isActive: Boolean): Result<Unit>
    suspend fun linkStaffToFlat(staffId: String, flatId: String): Result<Unit>
    suspend fun unlinkStaffFromFlat(staffId: String, flatId: String): Result<Unit>
}

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper
) : StaffRepository {

    override suspend fun addStaff(staff: ServiceStaff): Result<Unit> {
        return try {
            val docRef = db.getStaffRef().document()
            val finalStaff = staff.copy(id = docRef.id)
            docRef.set(finalStaff).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStaffList(societyId: String): List<ServiceStaff> {
        if (societyId.isBlank()) return emptyList()
        return try {
            val snapshot = db.getStaffRef()
                .whereEqualTo("societyId", societyId)
                .get().await()
            snapshot.toObjects(ServiceStaff::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateStaffStatus(staffId: String, isActive: Boolean): Result<Unit> {
        if (staffId.isBlank()) return Result.failure(Exception("Staff ID is required"))
        return try {
            db.getStaffRef().document(staffId)
                .update("isActive", isActive)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkStaffToFlat(staffId: String, flatId: String): Result<Unit> {
        if (staffId.isBlank()) return Result.failure(Exception("Staff ID is required"))
        return try {
            db.getStaffRef().document(staffId)
                .update("employerFlatIds", FieldValue.arrayUnion(flatId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkStaffFromFlat(staffId: String, flatId: String): Result<Unit> {
        if (staffId.isBlank()) return Result.failure(Exception("Staff ID is required"))
        return try {
            db.getStaffRef().document(staffId)
                .update("employerFlatIds", FieldValue.arrayRemove(flatId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}