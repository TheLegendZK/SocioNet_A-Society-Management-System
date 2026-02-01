package com.zhd.socionet.data.model

import com.google.firebase.firestore.PropertyName

data class Society(
    @PropertyName("id") val id: String = "",
    val name: String = "",
    val address: String = "",
    val registrationNumber: String = "",
    val wings: List<String> = emptyList(), // ["A", "B", "C"]
    val adminUserIds: List<String> = emptyList(),
    val maintenanceRatePerSqFt: Double = 0.0,
    val gateClosingTime: String = "23:00"
)
