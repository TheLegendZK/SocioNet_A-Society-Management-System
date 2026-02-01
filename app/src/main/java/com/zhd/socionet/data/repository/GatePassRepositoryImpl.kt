package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.VisitorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface GatePassRepository {
    suspend fun createGatePass(entry: GateEntry): Result<String>

    // CHANGED: Now returns Flow (Live Stream)
    fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>>

    suspend fun getActivePasses(flatId: String): List<GateEntry>
    suspend fun getPastPasses(flatId: String): List<GateEntry>
    suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit>
    fun getStaffLogsFlow(staffIds: List<String>): Flow<List<GateEntry>>
    suspend fun logStaffEntry(staffId: String, staffName: String, action: String): Result<Unit>
}

@Singleton
class GatePassRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GatePassRepository {

    override suspend fun createGatePass(entry: GateEntry): Result<String> {
        return try {
            val docRef = firestore.collection("gate_entries").document()
            val finalEntry = entry.copy(id = docRef.id, qrCodeUrl = docRef.id)
            docRef.set(finalEntry).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REAL TIME LISTENER ---
    override fun getPendingRequestsFlow(flatId: String): Flow<List<GateEntry>> {
        return firestore.collection("gate_entries")
            .whereEqualTo("hostFlatId", flatId)
            .whereEqualTo("status", EntryStatus.PENDING_APPROVAL.name)
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .snapshots() // <--- Magic function for Realtime updates
            .map { snapshot ->
                snapshot.toObjects(GateEntry::class.java)
            }
    }

    override suspend fun getActivePasses(flatId: String): List<GateEntry> {
        val snapshot = firestore.collection("gate_entries")
            .whereEqualTo("hostFlatId", flatId)
            .whereEqualTo("status", EntryStatus.APPROVED.name)
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .get().await()
        return snapshot.toObjects(GateEntry::class.java)
    }

    override suspend fun getPastPasses(flatId: String): List<GateEntry> {
        val snapshot = firestore.collection("gate_entries")
            .whereEqualTo("hostFlatId", flatId)
            .whereIn("status", listOf(EntryStatus.CHECKED_IN.name, EntryStatus.CHECKED_OUT.name, EntryStatus.DENIED.name, EntryStatus.EXPIRED.name))
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(50)
            .get().await()
        return snapshot.toObjects(GateEntry::class.java)
    }

    override suspend fun respondToRequest(entryId: String, status: EntryStatus): Result<Unit> {
        return try {
            firestore.collection("gate_entries").document(entryId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getStaffLogsFlow(staffIds: List<String>): Flow<List<GateEntry>> {
        if (staffIds.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())

        // Firestore 'whereIn' limits to 10 items.
        // For a single flat, having >10 staff is rare, so this is safe.
        return firestore.collection("gate_entries")
            .whereIn("linkedStaffId", staffIds)
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .limit(20)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(GateEntry::class.java) }
    }

    override suspend fun logStaffEntry(staffId: String, staffName: String, action: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("gate_entries").document()

            // Logic: If Action is IN, we create a new Entry.
            // If OUT, we ideally find the last IN and update it, but for simple logging,
            // we just create a record saying "Status: CHECKED_OUT".

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
            Result.failure(e)
        }
    }
}