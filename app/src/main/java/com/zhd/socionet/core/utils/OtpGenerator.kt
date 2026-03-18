package com.zhd.socionet.core.utils

import java.security.SecureRandom
import javax.inject.Inject

interface OtpGenerator {
    fun generateOtp(): String
}

class OtpGeneratorImpl @Inject constructor() : OtpGenerator {

    private val secureRandom = SecureRandom()

    override fun generateOtp(): String {
        // Generates a number between 0 and 999999
        val number = secureRandom.nextInt(1000000)
        // Formats it to 6 digits with leading zeros (e.g., 5 -> "000005")
        return String.format("%06d", number)
    }
}
