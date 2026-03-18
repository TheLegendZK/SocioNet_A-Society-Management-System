package com.zhd.socionet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val photoUrl: String?,
    val category: String,
    val scope: String,
    val status: String,
    val raisedByUserId: String,
    val raisedByUserName: String?,
    val flatId: String,
    val flatNumber: String?,
    val createdAt: Long
)
