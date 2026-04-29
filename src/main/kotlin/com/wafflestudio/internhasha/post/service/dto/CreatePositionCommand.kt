package com.wafflestudio.internhasha.post.service.dto

import com.wafflestudio.internhasha.post.domain.Category
import java.time.LocalDateTime

class CreatePositionCommand(
    val positionTitle: String,
    val positionType: Category,
    val detail: String,
    val headCount: Long,
    val salary: Long? = null,
    val employmentEndDate: LocalDateTime? = null,
    val companyId: String,
)

typealias UpdatePositionCommand = CreatePositionCommand
