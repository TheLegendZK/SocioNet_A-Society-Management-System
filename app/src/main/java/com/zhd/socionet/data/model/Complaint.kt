package com.zhd.socionet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Complaint(
    @PropertyName("id") val id: String = "",
    val title: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val category: ComplaintCategory = ComplaintCategory.OTHER,
    val scope: ComplaintScope = ComplaintScope.PERSONAL,
    
    // Denormalized User Info
    val raisedByUserId: String = "",
    val raisedByUserName: String? = null,
    
    // Denormalized Flat Info
    val flatId: String? = null,
    val flatNumber: String? = null,

    val status: String = "OPEN",
    val createdAt: Timestamp = Timestamp.now(),
    val resolvedAt: Timestamp? = null
)
