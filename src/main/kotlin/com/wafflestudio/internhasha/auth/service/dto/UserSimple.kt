package com.wafflestudio.internhasha.auth.service.dto

import com.wafflestudio.internhasha.auth.domain.UserRole

data class UserSimple(
    val id: String,
    val userRole: UserRole,
)
