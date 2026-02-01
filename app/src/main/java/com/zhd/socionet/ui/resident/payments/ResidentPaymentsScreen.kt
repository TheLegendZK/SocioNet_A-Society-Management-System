package com.zhd.socionet.ui.resident.payments

import androidx.compose.runtime.Composable
import com.zhd.socionet.ui.billing.BillingScreen

@Composable
fun ResidentPaymentsScreen(flatId: String, userId: String, onBack: () -> Unit) {
    BillingScreen(flatId = flatId, userId = userId, isAdmin = false, onBack = onBack)
}