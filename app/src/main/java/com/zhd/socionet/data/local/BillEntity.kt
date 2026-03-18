package com.zhd.socionet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String,
    val flatId: String,
    val societyId: String, // Store societyId for filtering
    val flatNumber: String?,
    val month: String,
    val amountDue: Double,
    val isPaid: Boolean,
    val billDate: Long,
    val dueDate: Long
)
