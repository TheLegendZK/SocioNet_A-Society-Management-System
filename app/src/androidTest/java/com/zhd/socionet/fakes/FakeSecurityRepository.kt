package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.repository.SecurityRepository

class FakeSecurityRepository : SecurityRepository {
    override suspend fun verifyEntryByOtp(otp: String): Result<GateEntry> = Result.failure(Exception("Not implemented"))
    override suspend fun verifyEntryByQr(passId: String): Result<GateEntry> = Result.failure(Exception("Not implemented"))
    override suspend fun markEntryAsCheckedIn(entryId: String, guardId: String): Result<Unit> = Result.success(Unit)
}
