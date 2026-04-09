package com.wafflestudio.internhasha.common.event

import com.wafflestudio.internhasha.auth.domain.UserRole

data class UserDeletedEvent(
    val userId: String,
    val userRole: UserRole,
)
