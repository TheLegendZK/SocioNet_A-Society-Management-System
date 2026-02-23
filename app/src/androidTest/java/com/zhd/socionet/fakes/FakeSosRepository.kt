package com.zhd.socionet.fakes

import com.zhd.socionet.data.repository.SosRepository

class FakeSosRepository : SosRepository {
    override suspend fun triggerSos(userId: String, flatId: String, type: String): Result<Unit> {
        return Result.success(Unit)
    }
}
