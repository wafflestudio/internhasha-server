package com.wafflestudio.internhasha.auth.controller.dto.response

import com.wafflestudio.internhasha.auth.domain.UserRole

data class UserResponse(
    val id: String,
    val userRole: UserRole,
)
