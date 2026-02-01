package com.zhd.socionet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Notice(
    @PropertyName("id") val id: String = "",
    val title: String = "",
    val content: String = "",
    val attachmentUrl: String? = null,
    val priority: String = "NORMAL",
    val targetAudience: String = "ALL", // "ALL", "OWNERS", "WING_A"
    val postedByAdminId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
)
