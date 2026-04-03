package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SecurityRepository {
    suspend fun verifyEntryByOtp(otp: String, societyId: String): Result<GateEntry>
    suspend fun verifyEntryByQr(passId: String, societyId: String): Result<GateEntry>
    suspend fun markEntryAsCheckedIn(entryId: String, guardId: String): Result<Unit>
}

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper
) : SecurityRepository {

    override suspend fun verifyEntryByOtp(otp: String, societyId: String): Result<GateEntry> {
        return try {
            val snapshot = db.getGateEntriesRef()
                .whereEqualTo("societyId", societyId)
                .whereEqualTo("entryCode", otp)
                .whereIn("status", listOf(EntryStatus.APPROVED.name, EntryStatus.PENDING_APPROVAL.name))
                .get().await()

            if (snapshot.isEmpty) {
                Result.failure(Exception("Invalid or Expired OTP"))
            } else {
                val entry = snapshot.documents.first().toObject(GateEntry::class.java)!!
                Result.success(entry)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyEntryByQr(passId: String, societyId: String): Result<GateEntry> {
        return try {
            val doc = db.getGateEntriesRef().document(passId).get().await()
            if (!doc.exists()) {
                Result.failure(Exception("Invalid QR Code"))
            } else {
                val entry = doc.toObject(GateEntry::class.java)!!
                if (entry.societyId != societyId) {
                    Result.failure(Exception("Pass belongs to another Society"))
                } else if (entry.status != EntryStatus.APPROVED && entry.status != EntryStatus.PENDING_APPROVAL) {
                    Result.failure(Exception("Pass is ${entry.status} (Not Valid for Entry)"))
                } else {
                    Result.success(entry)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markEntryAsCheckedIn(entryId: String, guardId: String): Result<Unit> {
        return try {
            db.getGateEntriesRef().document(entryId)
                .update(
                    mapOf(
                        "status" to EntryStatus.CHECKED_IN,
                        "entryTime" to Timestamp.now(),
                        "scannedByGuardId" to guardId
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}