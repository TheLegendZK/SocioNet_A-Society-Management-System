package com.zhd.socionet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val societyId: String,
    val employerFlatIds: List<String>
)
