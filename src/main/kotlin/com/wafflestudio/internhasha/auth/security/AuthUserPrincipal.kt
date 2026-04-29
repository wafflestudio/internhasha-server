package com.wafflestudio.internhasha.auth.security

import com.wafflestudio.internhasha.auth.domain.UserRole

data class AuthUserPrincipal(
    val id: String,
    val name: String,
    val userRole: UserRole,
    val email: String,
    val passwordHash: String? = null,
)
