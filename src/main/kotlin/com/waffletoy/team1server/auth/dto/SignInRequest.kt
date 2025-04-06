package com.waffletoy.team1server.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignInRequest(
    @field:NotBlank(message = "mail is required")
    @field:Email(message = "Invalid email format.")
    val email: String,
    @field:NotBlank(message = "password is required")
    @field:NotBlank(message = "password is required")
    @field:Size(
        min = 8,
        max = 64,
        message = "Password must be between 8 and 64 characters.",
    )
    val password: String,
)
