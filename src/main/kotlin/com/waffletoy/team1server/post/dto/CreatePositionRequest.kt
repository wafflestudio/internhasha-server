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
    @field:NotBlank(message = "Headcount is required.")
    val headcount: String,
    val employmentEndDate: LocalDateTime? = null,
    val isActive: Boolean? = false,
    val companyId: String? = null,
)

typealias UpdatePositionRequest = CreatePositionRequest
