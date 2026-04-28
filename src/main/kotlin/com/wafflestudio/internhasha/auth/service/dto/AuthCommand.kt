package com.wafflestudio.internhasha.auth.service.dto

import com.wafflestudio.internhasha.auth.domain.UserRole

data class SignInCommand(
    val email: String,
    val password: String,
)

data class CheckDuplicateMailCommand(
    val email: String,
)

data class SendSnuMailVerificationCommand(
    val snuMail: String,
)

data class CheckSnuMailVerificationCommand(
    val snuMail: String,
    val code: String,
)

data class ChangePasswordCommand(
    val userId: String,
    val oldPassword: String,
    val newPassword: String,
)

data class ResetPasswordCommand(
    val email: String,
)

data class SignUpCommand(
    val authType: UserRole,
    val info: SignupInfo,
) {
    sealed interface SignupInfo

    data class LocalApplicantInfo(
        val name: String,
        val email: String,
        val password: String,
        val successCode: String,
    ) : SignupInfo

    data class LocalCompanyInfo(
        val secretPassword: String,
        val name: String,
        val email: String,
        val password: String,
        val vcName: String? = null,
        val vcRecommendation: String? = null,
    ) : SignupInfo
}
