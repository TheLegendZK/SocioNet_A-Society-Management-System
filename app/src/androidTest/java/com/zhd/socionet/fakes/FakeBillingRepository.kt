package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.data.repository.BillingRepository

class FakeBillingRepository : BillingRepository {
    override suspend fun generateMonthlyBills(societyId: String, monthName: String): Result<Int> = Result.success(10)
    override suspend fun getBillsForFlat(flatId: String): List<Bill> = emptyList()
    override suspend fun payBill(billId: String, userId: String): Result<Unit> = Result.success(Unit)
}
