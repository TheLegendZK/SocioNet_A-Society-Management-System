package com.zhd.socionet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zhd.socionet.core.constants.AppConstants
import com.zhd.socionet.data.local.BillDao
import com.zhd.socionet.data.local.BillEntity
import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface BillingRepository {
    suspend fun generateMonthlyBills(societyId: String, monthName: String): Result<Int>
    suspend fun getBillsForFlat(flatId: String): List<Bill>
    suspend fun payBill(billId: String, userId: String): Result<Unit>
}

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dao: BillDao,
    private val crashlytics: FirebaseCrashlytics
) : BillingRepository {

    override suspend fun generateMonthlyBills(societyId: String, monthName: String): Result<Int> {
        if (societyId.isBlank()) return Result.failure(Exception("Society ID is required"))
        return try {
            // 1. Fetch Society Rules (Rate per SqFt)
            val societyDoc = firestore.collection(AppConstants.COLL_SOCIETIES).document(societyId).get().await()
            val society = societyDoc.toObject(Society::class.java)
                ?: throw Exception("Society not found")

            if (society.maintenanceRatePerSqFt <= 0) {
                throw Exception("Maintenance Rate is not set in Society Settings")
            }

            // 2. Fetch All Flats
            val flatsSnapshot = firestore.collection(AppConstants.COLL_FLATS)
                .whereEqualTo("societyId", societyId)
                .get().await()
            val flats = flatsSnapshot.toObjects(Flat::class.java)

            // 3. Batch Write Bills
            val batch = firestore.batch()
            var count = 0

            // Set Due Date to 10 days from now
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 10)
            val dueDate = Timestamp(calendar.time)
            val billDate = Timestamp.now()

            flats.forEach { flat ->
                if (flat.sqFtArea > 0) { // Only bill configured flats
                    val amount = flat.sqFtArea * society.maintenanceRatePerSqFt

                    val billRef = firestore.collection(AppConstants.COLL_BILLS).document()
                    val bill = Bill(
                        id = billRef.id,
                        flatId = flat.id,
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
            val snapshot = firestore.collection(AppConstants.COLL_BILLS)
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

    override suspend fun payBill(billId: String, userId: String): Result<Unit> {
        if (billId.isBlank()) return Result.failure(Exception("Bill ID is required"))
        return try {
            firestore.collection(AppConstants.COLL_BILLS).document(billId)
                .update("isPaid", true, "paidByUserId", userId, "paymentTransactionId", "TXN_${System.currentTimeMillis()}")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}

fun Bill.toEntity() =
    BillEntity(id, flatId, month, amountDue, isPaid, billDate.toDate().time, dueDate.toDate().time)

fun BillEntity.toDomain() = Bill(
    id, flatId, month, Timestamp(Date(billDate)), Timestamp(
        Date(
            dueDate
        )
    ), amountDue, 0.0, isPaid
)