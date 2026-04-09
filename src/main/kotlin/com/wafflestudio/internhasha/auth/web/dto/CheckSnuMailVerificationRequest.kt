package com.wafflestudio.internhasha.auth.web.dto

import com.wafflestudio.internhasha.auth.application.dto.CheckSnuMailVerificationCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CheckSnuMailVerificationRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
    @field:NotBlank(message = "Verification code is required")
    val code: String,
) {
    fun toCommand() =
        CheckSnuMailVerificationCommand(
            snuMail = this.snuMail,
            code = this.code,
        )
}
