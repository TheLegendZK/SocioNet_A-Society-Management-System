package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.EmergencyAlert
import com.zhd.socionet.data.repository.SosRepository

class FakeSosRepository : SosRepository {
    override suspend fun triggerSos(alert: EmergencyAlert): Result<Unit> {
        return Result.success(Unit)
    }
}
