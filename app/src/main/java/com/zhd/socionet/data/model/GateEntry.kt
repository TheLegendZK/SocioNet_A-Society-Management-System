package com.zhd.socionet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class GateEntry(
    @PropertyName("id") val id: String = "",
    val visitorName: String = "",
    val visitorType: VisitorType = VisitorType.GUEST,
    val vehicleNumber: String? = null,
    val hostFlatId: String = "",
    val hostFlatNumber: String? = null,
    val hostUserId: String = "",
    val entryCode: String = "", // 6-digit OTP
    val qrCodeUrl: String? = null,
    val scannedByGuardId: String? = null,
    val entryTime: Timestamp? = null,
    val exitTime: Timestamp? = null,
    val status: EntryStatus = EntryStatus.PENDING_APPROVAL,
    val linkedStaffId: String? = null,
    @get:PropertyName("isStaffEntry") @set:PropertyName("isStaffEntry")
    var isStaffEntry: Boolean = false
)
