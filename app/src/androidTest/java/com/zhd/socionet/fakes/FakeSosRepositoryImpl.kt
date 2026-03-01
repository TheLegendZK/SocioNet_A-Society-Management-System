package com.zhd.socionet.fakes

import com.zhd.socionet.data.model.EmergencyAlert
import com.zhd.socionet.data.repository.SosRepository

class FakeSosRepositoryImpl : SosRepository {
    override suspend fun triggerSos(alert: EmergencyAlert): Result<Unit> = Result.success(Unit)
}
