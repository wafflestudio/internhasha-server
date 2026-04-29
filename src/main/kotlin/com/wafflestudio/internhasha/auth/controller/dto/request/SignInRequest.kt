package com.wafflestudio.internhasha.auth.controller.dto.request

import com.wafflestudio.internhasha.auth.service.dto.SignInCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignInRequest(
    @field:NotBlank(message = "mail is required")
    @field:Email(message = "Invalid email format.")
    val email: String,
    @field:NotBlank(message = "password is required")
    @field:Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters.")
    val password: String,
) {
    fun toCommand() =
        SignInCommand(
            email = this.email,
            password = this.password,
        )
}
