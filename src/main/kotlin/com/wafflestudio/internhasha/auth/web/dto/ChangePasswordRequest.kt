package com.wafflestudio.internhasha.auth.web.dto

import com.wafflestudio.internhasha.auth.application.dto.ChangePasswordCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "password is required")
    @field:Size(
        min = 8,
        max = 64,
        message = "Password must be between 8 and 64 characters.",
    )
    val oldPassword: String,
    @field:NotBlank(message = "password is required")
    @field:Size(
        min = 8,
        max = 64,
        message = "Password must be between 8 and 64 characters.",
    )
    val newPassword: String,
) {
    fun toCommand(userId: String) =
        ChangePasswordCommand(
            userId = userId,
            oldPassword = this.oldPassword,
            newPassword = this.newPassword,
        )
}
