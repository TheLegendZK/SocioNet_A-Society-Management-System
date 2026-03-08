package com.zhd.socionet.data.model

import com.google.firebase.firestore.PropertyName

data class Society(
    @PropertyName("id") val id: String = "",
    val name: String = "",
    val address: String = "",
    val registrationNumber: String = "",
    val wings: List<String> = emptyList(), // ["A", "B", "C"]
    val wingConfigs: List<WingConfigData> = emptyList(),
    val adminUserIds: List<String> = emptyList(),
    val maintenanceRatePerSqFt: Double = 0.0,
    val gateClosingTime: String = "23:00"
)

data class WingConfigData(
    val wingName: String = "",
    val floors: Int = 0,
    val flatsPerFloor: Int = 0
)