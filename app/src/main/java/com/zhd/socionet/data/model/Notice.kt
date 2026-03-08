package com.zhd.socionet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Notice(
    @PropertyName("id") val id: String = "",
    val societyId: String = "",
    val title: String = "",
    val content: String = "",
    val attachmentUrl: String? = null,
    val priority: String = "NORMAL",
    val targetAudience: String = "ALL", // "ALL", "OWNERS", "WING_A"
    
    // Denormalized Author Info
    val postedByAdminId: String = "",
    val authorName: String? = null,

    val timestamp: Timestamp = Timestamp.now(),
)
