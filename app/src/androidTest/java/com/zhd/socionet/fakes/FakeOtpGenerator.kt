package com.zhd.socionet.fakes

import com.zhd.socionet.core.utils.OtpGenerator

class FakeOtpGenerator : OtpGenerator {
    override fun generateOtp(): String = "123456"
}
