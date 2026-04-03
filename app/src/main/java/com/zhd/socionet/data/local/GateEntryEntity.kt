package com.zhd.socionet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gate_entries")
data class GateEntryEntity(
    @PrimaryKey val id: String,
    val societyId: String,
    val visitorName: String,
    val visitorType: String,
    val hostFlatId: String,
    val hostFlatNumber: String?,
    val status: String,
    val entryCode: String,
    val entryTime: Long,
    val isStaffEntry: Boolean,
    val linkedStaffId: String?
)
