package com.wafflestudio.internhasha.auth.application.port

interface VerificationCodePort {
    fun saveEmailCode(
        email: String,
        emailCode: String,
    )

    fun verifyEmailCode(
        email: String,
        emailCode: String,
    ): Boolean

    fun deleteEmailCode(email: String): Boolean

    fun saveSuccessCode(
        email: String,
        successCode: String,
    )

    fun verifySuccessCode(
        email: String,
        successCode: String,
    ): Boolean

    fun deleteSuccessCode(email: String): Boolean
}
