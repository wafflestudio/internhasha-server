package com.waffletoy.team1server.user.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignInRequest(
    @field:NotBlank(message = "authType is required")
    val authType: AuthType,
    @field:Valid
    val info: Info,
) {
    enum class AuthType {
        LOCAL,
        SOCIAL,
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Info.LocalInfo::class, name = "LOCAL"),
        JsonSubTypes.Type(value = Info.SocialInfo::class, name = "SOCIAL"),
    )
    sealed class Info {
        data class LocalInfo(
            @field:NotBlank(message = "localLoginId is required")
            @field:Pattern(
                regexp = "^[a-zA-Z][a-zA-Z0-9_-]{4,19}$",
            )
            val localLoginId: String,
            @field:NotBlank(message = "password is required")
            @field:Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,20}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character",
            )
            val password: String,
        ) : Info()

        data class SocialInfo(
            @field:NotBlank(message = "provider is required")
            val provider: String,
            @field:NotBlank(message = "token is required")
            val token: String,
        ) : Info()
    }
}
