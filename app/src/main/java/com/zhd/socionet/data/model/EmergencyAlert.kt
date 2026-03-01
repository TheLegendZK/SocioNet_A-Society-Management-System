package com.zhd.socionet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class EmergencyAlert(
    @PropertyName("id") val id: String = "",
    val type: String = "SOS",
    val subType: String = "",
    val status: String = "ACTIVE",
    val timestamp: Timestamp = Timestamp.now(),
    
    // Denormalized User Info
    val raisedByUserId: String = "",
    val raisedByUserName: String? = null,
    
    // Denormalized Flat Info
    val flatId: String = "",
    val flatNumber: String? = null,
    
    val message: String = ""
)
