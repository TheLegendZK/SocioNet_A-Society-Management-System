package com.zhd.socionet.data.model

import com.google.firebase.firestore.PropertyName

data class Flat(
    @PropertyName("id") val id: String = "", // Format: "soc_id_Wing_A_101"
    val societyId: String = "",
    val wing: String = "",
    val floorNumber: Int = 0,
    val flatNumber: String = "",
    val sqFtArea: Int = 0,
    val parkingSlots: List<String> = emptyList(),

    // Denormalized Household Info
    val primaryOwnerId: String? = null,
    val primaryOwnerName: String? = null,

    val tenantIds: List<String> = emptyList(),
    val familyMemberIds: List<String> = emptyList(),

    // Status
    val isOccupied: Boolean = false,
    val hasOutstandingDues: Boolean = false
)
