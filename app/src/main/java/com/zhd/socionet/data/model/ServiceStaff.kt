package com.zhd.socionet.data.model

import com.google.firebase.firestore.PropertyName

data class ServiceStaff(
    @PropertyName("id") val id: String = "",
    val name: String = "",
    val role: String = "", // "Maid", "Driver", "Security"
    val phoneNumber: String = "",
    val photoUrl: String = "",
    @get:PropertyName("active") @set:PropertyName("active")
    var isActive: Boolean = true,
    val societyId: String = "", // Added field for filtering
    val employerFlatIds: List<String> = emptyList()
)
