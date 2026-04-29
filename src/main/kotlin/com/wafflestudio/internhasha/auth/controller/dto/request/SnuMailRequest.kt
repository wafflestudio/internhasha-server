package com.wafflestudio.internhasha.auth.controller.dto.request

import com.wafflestudio.internhasha.auth.service.dto.SendSnuMailVerificationCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SnuMailRequest(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@snu\\.ac\\.kr$", message = "Email must end with @snu.ac.kr")
    val snuMail: String,
) {
    fun toCommand() = SendSnuMailVerificationCommand(snuMail = this.snuMail)
}
