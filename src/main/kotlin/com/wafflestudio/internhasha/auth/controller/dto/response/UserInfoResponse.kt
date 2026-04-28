package com.wafflestudio.internhasha.auth.controller.dto.response

import com.wafflestudio.internhasha.auth.domain.UserRole

data class UserInfoResponse(
    val id: String,
    val name: String,
    val userRole: UserRole,
    val email: String,
)
