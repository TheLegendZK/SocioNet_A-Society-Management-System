package com.zhd.socionet.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpGeneratorTest {

    private val otpGenerator = OtpGeneratorImpl()

    @Test
    fun `generateOtp returns string of length 6`() {
        val otp = otpGenerator.generateOtp()
        assertEquals(6, otp.length)
    }

    @Test
    fun `generateOtp returns only numeric digits`() {
        val otp = otpGenerator.generateOtp()
        assertTrue("OTP should only contain digits", otp.all { it.isDigit() })
    }

    @Test
    fun `generateOtp produces different values on subsequent calls`() {
        // Very simplistic check for randomness failure (extremely unlikely to collide sequentially)
        val otp1 = otpGenerator.generateOtp()
        val otp2 = otpGenerator.generateOtp()

        // Note: There is a minuscule chance this fails if random generates same number twice,
        // but for a unit test verifying implementation it's acceptable.
        assertTrue("Subsequent OTPs should essentially differ", otp1 != otp2)
    }
}