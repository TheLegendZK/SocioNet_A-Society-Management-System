package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.Query
import com.zhd.socionet.data.local.BillDao
import com.zhd.socionet.data.local.BillEntity
import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import com.zhd.socionet.data.remote.FirestoreHelper
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface BillingRepository {
    suspend fun generateMonthlyBills(societyId: String, monthName: String): Result<Int>
    suspend fun getBillsForFlat(flatId: String): List<Bill>
    suspend fun getAllBills(societyId: String): List<Bill>
    suspend fun payBill(billId: String, userId: String): Result<Unit>
}

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val db: FirestoreHelper,
    private val dao: BillDao,
    private val crashlytics: FirebaseCrashlytics
) : BillingRepository {

    override suspend fun generateMonthlyBills(societyId: String, monthName: String): Result<Int> {
        if (societyId.isBlank()) return Result.failure(Exception("Society ID is required"))
        return try {
            // 1. Fetch Society Rules (Rate per SqFt + Fixed Charges)
            val societyDoc = db.getSocietiesRef().document(societyId).get().await()
            val society = societyDoc.toObject(Society::class.java)
                ?: throw Exception("Society not found")

            if (society.maintenanceRatePerSqFt <= 0 && society.fixedCharges <= 0) {
                throw Exception("Maintenance Rate or Fixed Charges must be set in Society Settings")
            }

            // 2. Fetch All Flats
            val flatsSnapshot = db.getFlatsRef()
                .whereEqualTo("societyId", societyId)
                .get().await()
            val flats = flatsSnapshot.toObjects(Flat::class.java)

            // 3. Batch Write Bills
            val batch = db.getFirestore().batch()
            var count = 0

            // Set Due Date to 10 days from now
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 10)
            val dueDate = Timestamp(calendar.time)
            val billDate = Timestamp.now()

            flats.forEach { flat ->
                if (flat.sqFtArea > 0 || society.fixedCharges > 0) { // Only bill if something is configured
                    val amount = (flat.sqFtArea * society.maintenanceRatePerSqFt) + society.fixedCharges

                    val billRef = db.getBillsRef().document()
                    val bill = Bill(
                        id = billRef.id,
                        flatId = flat.id,
                        societyId = societyId,
                        flatNumber = flat.flatNumber,
                        month = monthName,
                        billDate = billDate,
                        dueDate = dueDate,
                        amountDue = amount,
                        isPaid = false
                    )
                    batch.set(billRef, bill)
                    count++
                }
            }

            batch.commit().await()
            Result.success(count)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getBillsForFlat(flatId: String): List<Bill> {
        if (flatId.isBlank()) return emptyList()
        try {
            val snapshot = db.getBillsRef()
                .whereEqualTo("flatId", flatId)
                .orderBy("billDate", Query.Direction.DESCENDING)
                .get().await()
            val list = snapshot.toObjects(Bill::class.java)
            dao.insertBills(list.map { it.toEntity() })
        } catch (e: Exception) { 
            crashlytics.recordException(e)
        }

        return dao.getBillsForFlat(flatId).map { it.toDomain() }
    }

    override suspend fun getAllBills(societyId: String): List<Bill> {
        if (societyId.isBlank()) return emptyList()
        return try {
            val snapshot = db.getBillsRef()
                .whereEqualTo("societyId", societyId)
                .orderBy("billDate", Query.Direction.DESCENDING)
                .limit(100)
                .get().await()
            snapshot.toObjects(Bill::class.java)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    override suspend fun payBill(billId: String, userId: String): Result<Unit> {
        if (billId.isBlank()) return Result.failure(Exception("Bill ID is required"))
        return try {
            db.getBillsRef().document(billId)
                .update("isPaid", true, "paidByUserId", userId, "paymentTransactionId", "TXN_${System.currentTimeMillis()}")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}

fun Bill.toEntity() = BillEntity(
    id = id,
    flatId = flatId,
    societyId = societyId,
    flatNumber = flatNumber,
    month = month,
    amountDue = amountDue,
    isPaid = isPaid,
    billDate = billDate.toDate().time,
    dueDate = dueDate.toDate().time
)

fun BillEntity.toDomain() = Bill(
    id = id,
    flatId = flatId,
    societyId = societyId,
    flatNumber = flatNumber,
    month = month,
    billDate = Timestamp(Date(billDate)),
    dueDate = Timestamp(Date(dueDate)),
    amountDue = amountDue,
    penaltyAmount = 0.0,
    isPaid = isPaid
)