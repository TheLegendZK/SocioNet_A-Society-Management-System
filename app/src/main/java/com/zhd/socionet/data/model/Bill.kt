package com.zhd.socionet.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Bill(
    @PropertyName("id") val id: String = "",
    val flatId: String = "",
    val societyId: String = "", // Added societyId for easier filtering
    val flatNumber: String? = null, // Denormalized for Admin View

    val month: String = "", // e.g., "January 2026"
    val billDate: Timestamp = Timestamp.now(),
    val dueDate: Timestamp = Timestamp.now(),

    // Calculation
    val amountDue: Double = 0.0,
    val penaltyAmount: Double = 0.0, // Late fees logic for future

    // Payment Status
    val isPaid: Boolean = false,
    val paymentTransactionId: String? = null, // "TXN_12345"
    val paidByUserId: String? = null // Who actually paid it?
)
