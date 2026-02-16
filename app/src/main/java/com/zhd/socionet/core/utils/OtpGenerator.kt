package com.zhd.socionet.core.utils

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

interface OtpGenerator {
    fun generateOtp(length: Int = 6): String
}

@Singleton
class OtpGeneratorImpl @Inject constructor() : OtpGenerator {
    override fun generateOtp(length: Int): String {
        val min = Math.pow(10.0, (length - 1).toDouble()).toInt()
        val max = Math.pow(10.0, length.toDouble()).toInt() - 1
        return Random.nextInt(min, max).toString()
    }
}
