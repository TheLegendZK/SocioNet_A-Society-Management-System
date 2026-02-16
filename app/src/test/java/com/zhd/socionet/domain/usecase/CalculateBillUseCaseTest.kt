package com.zhd.socionet.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBillUseCaseTest {

    private val calculateBillUseCase = CalculateBillUseCase()

    @Test
    fun `invoke returns correct amount for standard input`() {
        val sqFt = 1000
        val rate = 2.5

        val result = calculateBillUseCase(sqFt, rate)

        assertEquals(2500.0, result, 0.01)
    }

    @Test
    fun `invoke returns zero when area is zero`() {
        val sqFt = 0
        val rate = 5.0

        val result = calculateBillUseCase(sqFt, rate)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `invoke returns zero when rate is zero`() {
        val sqFt = 1500
        val rate = 0.0

        val result = calculateBillUseCase(sqFt, rate)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `invoke handles floating point precision correctly`() {
        val sqFt = 1250
        val rate = 3.33

        val result = calculateBillUseCase(sqFt, rate)

        // 1250 * 3.33 = 4162.5
        assertEquals(4162.5, result, 0.01)
    }
}