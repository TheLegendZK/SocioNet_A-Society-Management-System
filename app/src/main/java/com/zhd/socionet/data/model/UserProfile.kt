package com.zhd.socionet.data.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    @PropertyName("id") val id: String = "", // Firebase Auth ID
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val profilePhotoUrl: String = "",
    val fcmToken: String = "",
    val roles: List<UserRole> = emptyList(),

    // Residency Details (Denormalized for efficient queries)
    val societyId: String? = null,
    val societyName: String? = null,
    val wingId: String? = null,
    val flatId: String? = null,
    val flatNumber: String? = null,

    // FIX: Ensure boolean is mapped correctly
    @get:PropertyName("verified") @set:PropertyName("verified")
    var isVerified: Boolean = false
)
