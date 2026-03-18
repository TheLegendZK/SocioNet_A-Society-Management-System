package com.zhd.socionet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val attachmentUrl: String?,
    val priority: String,
    val targetAudience: String,
    val postedByAdminId: String,
    val authorName: String?,
    val timestamp: Long
)
