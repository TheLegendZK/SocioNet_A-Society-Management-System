package com.zhd.socionet.domain.usecase

import javax.inject.Inject

/**
 * Pure logic to calculate maintenance bills.
 * This makes it easy to change the formula later (e.g., adding taxes).
 */
class CalculateBillUseCase @Inject constructor() {

    operator fun invoke(sqFt: Int, ratePerSqFt: Double): Double {
        val baseAmount = sqFt * ratePerSqFt

        // Example: Add 18% GST Logic here in the future
        // val tax = baseAmount * 0.18
        // return baseAmount + tax

        return baseAmount
    }
}