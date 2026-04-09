package com.wafflestudio.internhasha.post.domain

import com.wafflestudio.internhasha.company.domain.Company
import java.time.LocalDateTime

data class Position(
    val id: String,
    val company: Company,
    val positionTitle: String,
    val positionType: Category,
    val headCount: Long,
    val salary: Long? = null,
    val detail: String,
    val employmentEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
