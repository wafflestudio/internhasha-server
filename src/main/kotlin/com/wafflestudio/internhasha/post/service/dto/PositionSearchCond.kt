package com.wafflestudio.internhasha.post.service.dto

import java.time.LocalDateTime

data class PositionSearchCond(
    val positions: List<String>?,
    val order: Int,
    val companyId: String?,
    val currentDateTime: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean,
    val domains: List<String>?,
)
