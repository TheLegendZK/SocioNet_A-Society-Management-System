package com.zhd.socionet.core.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    /**
     * Formats a double amount into currency format.
     * Currently set to Indian Rupees (INR) as default.
     */
    fun formatCurrency(amount: Double): String {
        return try {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).format(amount)
        } catch (e: Exception) {
            "₹$amount"
        }
    }
}
