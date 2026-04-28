package com.wafflestudio.internhasha.auth.controller.dto.request

import com.wafflestudio.internhasha.auth.service.dto.CheckDuplicateMailCommand
import com.wafflestudio.internhasha.auth.service.dto.ResetPasswordCommand
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
