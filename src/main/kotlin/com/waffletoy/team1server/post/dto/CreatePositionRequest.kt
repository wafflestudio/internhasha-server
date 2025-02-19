package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Category
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreatePositionRequest(
    @field:NotBlank(message = "Position title is required.")
    val title: String,
    @field:NotNull(message = "Category is required.")
    val category: Category,
    val detail: String? = null,
    @field:Max(value = 9999, message = "Headcount must not exceed 9999.")
    @field:Min(value = 0, message = "Headcount must be at least 0.")
    val headcount: Int = 0,
    val employmentEndDate: LocalDateTime? = null,
    val isActive: Boolean? = false,
    val companyId: String? = null,
)

typealias UpdatePositionRequest = CreatePositionRequest
