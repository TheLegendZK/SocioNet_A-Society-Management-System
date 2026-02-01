package com.zhd.socionet.data.model

enum class EntryStatus {
    PENDING_APPROVAL, // Waiting for resident to click "Approve"
    APPROVED,         // QR Scanned & Valid
    DENIED,           // Resident clicked "Deny"
    CHECKED_IN,       // Inside the premises
    CHECKED_OUT,      // Left the premises
    EXPIRED           // Pass wasn't used
}