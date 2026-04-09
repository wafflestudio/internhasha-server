package com.wafflestudio.internhasha.auth.web.dto

import com.wafflestudio.internhasha.auth.application.dto.CheckDuplicateMailCommand
import com.wafflestudio.internhasha.auth.application.dto.ResetPasswordCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailRequest(
    @field:NotBlank(message = "email is required")
    @field:Email(message = "email is required")
    val email: String,
) {
    fun toCheckDuplicateMailCommand() = CheckDuplicateMailCommand(email = this.email)

    fun toResetPasswordCommand() = ResetPasswordCommand(email = this.email)
}
