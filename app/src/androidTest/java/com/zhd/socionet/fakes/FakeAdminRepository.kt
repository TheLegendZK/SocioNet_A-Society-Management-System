package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import com.zhd.socionet.data.repository.AdminRepository

class FakeAdminRepository : AdminRepository {
    override suspend fun createSociety(society: Society): Result<String> = Result.success("SOC1")
    override suspend fun batchCreateFlats(flats: List<Flat>): Result<Unit> = Result.success(Unit)
}
