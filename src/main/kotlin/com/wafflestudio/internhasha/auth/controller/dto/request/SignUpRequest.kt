package com.wafflestudio.internhasha.auth.controller.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.service.dto.SignUpCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class SignUpRequest(
    val authType: UserRole,
    @field:JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "authType",
    )
    @field:JsonSubTypes(
        JsonSubTypes.Type(value = LocalApplicantInfo::class, name = RoleValue.ROLE_APPLICANT),
        JsonSubTypes.Type(value = LocalCompanyInfo::class, name = RoleValue.ROLE_COMPANY),
    )
    @field:Valid
    val info: SignupInfo,
) {
    fun toCommand() =
        SignUpCommand(
            authType = authType,
            info = info.toCommand(),
        )

    sealed interface SignupInfo {
        fun toCommand(): SignUpCommand.SignupInfo
    }

    data class LocalApplicantInfo(
        @field:NotBlank(message = "name is required")
        val name: String,
        @field:NotBlank(message = "snuMail is required")
        @field:Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$",
            message = "snuMail must be a valid SNU email address.",
        )
        val email: String,
        @field:NotBlank(message = "password is required")
        @field:Size(
            min = 8,
            max = 64,
            message = "Password must be between 8 and 64 characters.",
        )
        val password: String,
        val successCode: String,
    ) : SignupInfo {
        override fun toCommand() =
            SignUpCommand.LocalApplicantInfo(
                name = this.name,
                email = this.email,
                password = this.password,
                successCode = this.successCode,
            )
    }

    data class LocalCompanyInfo(
        @field:NotBlank(message = "secretPassword is required")
        val secretPassword: String,
        @field:NotBlank(message = "name is required")
        val name: String,
        @field:NotBlank(message = "email is required")
        @field:Email(message = "email is required")
        val email: String,
        @field:NotBlank(message = "password is required")
        @field:Size(
            min = 8,
            max = 64,
            message = "Password must be between 8 and 64 characters.",
        )
        val password: String,
        val vcName: String? = null,
        val vcRecommendation: String? = null,
    ) : SignupInfo {
        override fun toCommand() =
            SignUpCommand.LocalCompanyInfo(
                secretPassword = this.secretPassword,
                name = this.name,
                email = this.email,
                password = this.password,
                vcName = this.vcName,
                vcRecommendation = this.vcRecommendation,
            )
    }
}
