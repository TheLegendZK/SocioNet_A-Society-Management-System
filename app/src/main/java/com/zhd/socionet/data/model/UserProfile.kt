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
    val assignedFlatId: String? = null,

    // FIX: Ensure boolean is mapped correctly
    @get:PropertyName("verified") @set:PropertyName("verified")
    var isVerified: Boolean = false
)
