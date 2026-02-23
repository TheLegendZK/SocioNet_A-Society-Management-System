package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.VisitorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface GatePassRepository {
    suspend fun createGatePass(entry: GateEntry): Result<String>
    fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>>
    suspend fun getActivePasses(flatId: String): List<GateEntry>
    fun getPastPassesFlow(flatId: String): Flow<List<GateEntry>>
    suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit>
    fun getStaffLogsFlow(staffIds: List<String>): Flow<List<GateEntry>>
    suspend fun logStaffEntry(staffId: String, staffName: String, action: String): Result<Unit>
    suspend fun getUserByFlat(flatId: String): UserProfile?
    suspend fun getAllFlats(): Result<List<Flat>>
}

@Singleton
class GatePassRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val crashlytics: FirebaseCrashlytics
) : GatePassRepository {

    override suspend fun createGatePass(entry: GateEntry): Result<String> {
        return try {
            val docRef = firestore.collection(AppConstants.COLL_GATE_ENTRIES).document()
            val finalEntry = entry.copy(id = docRef.id, qrCodeUrl = docRef.id)
            docRef.set(finalEntry).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>> {
        return firestore.collection(AppConstants.COLL_GATE_ENTRIES)
            .whereEqualTo("hostFlatId", flatId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GateEntry::class.java)
                    .filter { it.status == EntryStatus.PENDING_APPROVAL }
                    .sortedByDescending { it.entryTime }
            }
    }

    override suspend fun getActivePasses(flatId: String): List<GateEntry> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_GATE_ENTRIES)
                .whereEqualTo("hostFlatId", flatId)
                .get().await()

            snapshot.toObjects(GateEntry::class.java)
                .filter { it.status == EntryStatus.APPROVED }
                .sortedByDescending { it.entryTime }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    override fun getPastPassesFlow(flatId: String): Flow<List<GateEntry>> {
        return firestore.collection(AppConstants.COLL_GATE_ENTRIES)
            .whereEqualTo("hostFlatId", flatId)
            .snapshots()
            .map { snapshot ->
                val pastStatuses = listOf(
                    EntryStatus.APPROVED,
                    EntryStatus.CHECKED_IN,
                    EntryStatus.CHECKED_OUT,
                    EntryStatus.DENIED,
                    EntryStatus.EXPIRED
                )
                snapshot.toObjects(GateEntry::class.java)
                    .filter { it.status in pastStatuses }
                    .sortedByDescending { it.entryTime }
                    .take(50)
            }
    }

    override suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit> {
        return try {
            firestore.collection(AppConstants.COLL_GATE_ENTRIES).document(entryId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override fun getStaffLogsFlow(staffIds: List<String>): Flow<List<GateEntry>> {
        if (staffIds.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())

        return firestore.collection(AppConstants.COLL_GATE_ENTRIES)
            .whereIn("linkedStaffId", staffIds)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GateEntry::class.java)
                    .sortedByDescending { it.entryTime }
                    .take(20)
            }
    }

    override suspend fun logStaffEntry(
        staffId: String,
        staffName: String,
        action: String,
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection(AppConstants.COLL_GATE_ENTRIES).document()
            val status = if (action == "IN") EntryStatus.CHECKED_IN else EntryStatus.CHECKED_OUT

            val entry = GateEntry(
                id = docRef.id,
                visitorName = staffName,
                visitorType = VisitorType.SERVICE_STAFF,
                isStaffEntry = true,
                linkedStaffId = staffId,
                status = status,
                entryTime = Timestamp.now()
            )

            docRef.set(entry).await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getUserByFlat(flatId: String): UserProfile? {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_USERS)
                .whereEqualTo("assignedFlatId", flatId)
                .limit(1)
                .get()
                .await()
            snapshot.toObjects(UserProfile::class.java).firstOrNull()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            null
        }
    }

    override suspend fun getAllFlats(): Result<List<Flat>> {
        return try {
            val snapshot = firestore.collection(AppConstants.COLL_FLATS).get().await()
            val flats = snapshot.toObjects(Flat::class.java)
            Result.success(flats)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}