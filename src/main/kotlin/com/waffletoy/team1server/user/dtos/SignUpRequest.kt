package com.waffletoy.team1server.user.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotNull(message = "authType is required")
    val authType: AuthType,
    @field:Valid
    val info: Info,
) {
    enum class AuthType {
        LOCAL_NORMAL,
        SOCIAL_NORMAL,
        LOCAL_CURATOR,
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true,
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = LocalNormalInfo::class, name = "LOCAL_NORMAL"),
        JsonSubTypes.Type(value = SocialNormalInfo::class, name = "SOCIAL_NORMAL"),
        JsonSubTypes.Type(value = LocalCuratorInfo::class, name = "LOCAL_CURATOR"),
    )
    sealed class Info

    @JsonTypeName("LOCAL_NORMAL")
    data class LocalNormalInfo(
        @field:NotBlank(message = "name is required")
        val name: String,
        @field:NotBlank(message = "localLoginId is required")
        @field:Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9_-]{4,19}$",
            message = "localLoginId must be between 5 and 20 characters, start with a letter, and contain only letters, numbers, underscores, or hyphens.",
        )
        val localLoginId: String,
        @field:NotBlank(message = "snuMail is required")
        @field:Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$",
            message = "snuMail must be a valid SNU email address.",
        )
        val snuMail: String,
        @field:NotBlank(message = "password is required")
        @field:Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,20}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.",
        )
        val password: String,
    ) : Info()

    @JsonTypeName("SOCIAL_NORMAL")
    data class SocialNormalInfo(
        @field:NotBlank(message = "provider is required")
        val provider: String,
        @field:NotBlank(message = "snuMail is required")
        @field:Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$",
            message = "snuMail must be a valid SNU email address.",
        )
        val snuMail: String,
        @field:NotBlank(message = "token is required")
        val token: String,
    ) : Info()

    @JsonTypeName("LOCAL_CURATOR")
    data class LocalCuratorInfo(
        @field:NotBlank(message = "secretPassword is required")
        val secretPassword: String,
        @field:NotBlank(message = "name is required")
        val name: String,
        @field:NotBlank(message = "localLoginId is required")
        val localLoginId: String,
        @field:NotBlank(message = "password is required")
        @field:Size(
            min = 8,
            max = 20,
            message = "Password must be between 8 and 20 characters.",
        )
        @field:Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!*]).{8,20}$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character.",
        )
        val password: String,
    ) : Info()
}
