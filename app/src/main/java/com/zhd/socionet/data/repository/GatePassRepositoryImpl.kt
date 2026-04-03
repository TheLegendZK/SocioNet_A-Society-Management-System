package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.snapshots
import com.zhd.socionet.data.local.GateEntryDao
import com.zhd.socionet.data.local.GateEntryEntity
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface GatePassRepository {
    suspend fun createGatePass(entry: GateEntry): Result<String>
    fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>>
    suspend fun getActivePasses(flatId: String): List<GateEntry>
    fun getPastPassesFlow(flatId: String): Flow<List<GateEntry>>
    suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit>
    fun getStaffLogsFlow(staffIds: List<String>): Flow<List<GateEntry>>
    suspend fun logStaffEntry(staffId: String, staffName: String, action: String, societyId: String): Result<Unit>
    suspend fun getUserByFlat(flatId: String): UserProfile?
    suspend fun getAllFlats(): Result<List<Flat>>
    fun getSecurityLogsFlow(societyId: String): Flow<List<GateEntry>>
}

@Singleton
class GatePassRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper,
    private val dao: GateEntryDao,
    private val crashlytics: FirebaseCrashlytics
) : GatePassRepository {

    override suspend fun createGatePass(entry: GateEntry): Result<String> {
        return try {
            val docRef = db.getGateEntriesRef().document()
            val finalEntry = entry.copy(id = docRef.id, qrCodeUrl = docRef.id)
            docRef.set(finalEntry).await()
            dao.insertEntries(listOf(finalEntry.toEntity()))
            Result.success(docRef.id)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>> {
        return db.getGateEntriesRef()
            .whereEqualTo("hostFlatId", flatId)
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(GateEntry::class.java)
                    .filter { it.status == EntryStatus.PENDING_APPROVAL }
                    .sortedByDescending { it.entryTime }
                dao.insertEntries(list.map { it.toEntity() })
                list
            }
    }

    override suspend fun getActivePasses(flatId: String): List<GateEntry> {
        return try {
            val snapshot = db.getGateEntriesRef()
                .whereEqualTo("hostFlatId", flatId)
                .get().await()

            val list = snapshot.toObjects(GateEntry::class.java)
                .filter { it.status == EntryStatus.APPROVED }
                .sortedByDescending { it.entryTime }
            dao.insertEntries(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    override fun getPastPassesFlow(flatId: String): Flow<List<GateEntry>> {
        return db.getGateEntriesRef()
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
                val list = snapshot.toObjects(GateEntry::class.java)
                    .filter { it.status in pastStatuses }
                    .sortedByDescending { it.entryTime }
                    .take(50)
                dao.insertEntries(list.map { it.toEntity() })
                list
            }
    }

    override suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit> {
        return try {
            db.getGateEntriesRef().document(entryId)
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

        return db.getGateEntriesRef()
            .whereIn("linkedStaffId", staffIds)
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(GateEntry::class.java)
                    .sortedByDescending { it.entryTime }
                    .take(20)
                dao.insertEntries(list.map { it.toEntity() })
                list
            }
    }

    override fun getSecurityLogsFlow(societyId: String): Flow<List<GateEntry>> {
        return db.getGateEntriesRef()
            .whereEqualTo("societyId", societyId)
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(GateEntry::class.java)
                    .sortedByDescending { it.entryTime }
                    .take(100)
                dao.insertEntries(list.map { it.toEntity() })
                list
            }
    }

    override suspend fun logStaffEntry(
        staffId: String,
        staffName: String,
        action: String,
        societyId: String
    ): Result<Unit> {
        return try {
            val docRef = db.getGateEntriesRef().document()
            val status = if (action == "IN") EntryStatus.CHECKED_IN else EntryStatus.CHECKED_OUT

            val entry = GateEntry(
                id = docRef.id,
                societyId = societyId,
                visitorName = staffName,
                visitorType = VisitorType.SERVICE_STAFF,
                isStaffEntry = true,
                linkedStaffId = staffId,
                status = status,
                entryTime = Timestamp.now()
            )

            docRef.set(entry).await()
            dao.insertEntries(listOf(entry.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getUserByFlat(flatId: String): UserProfile? {
        return try {
            val snapshot = db.getUsersRef()
                .whereEqualTo("flatId", flatId)
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
            val snapshot = db.getFlatsRef().get().await()
            val flats = snapshot.toObjects(Flat::class.java)
            Result.success(flats)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}

fun GateEntry.toEntity() = GateEntryEntity(
    id = id,
    societyId = societyId,
    visitorName = visitorName,
    visitorType = visitorType.name,
    hostFlatId = hostFlatId,
    hostFlatNumber = hostFlatNumber,
    status = status.name,
    entryCode = entryCode,
    entryTime = entryTime?.toDate()?.time ?: 0L,
    isStaffEntry = isStaffEntry,
    linkedStaffId = linkedStaffId
)

fun GateEntryEntity.toDomain() = GateEntry(
    id = id,
    societyId = societyId,
    visitorName = visitorName,
    visitorType = VisitorType.valueOf(visitorType),
    hostFlatId = hostFlatId,
    hostFlatNumber = hostFlatNumber,
    status = EntryStatus.valueOf(status),
    entryCode = entryCode,
    entryTime = if (entryTime > 0) Timestamp(Date(entryTime)) else null,
    isStaffEntry = isStaffEntry,
    linkedStaffId = linkedStaffId
)