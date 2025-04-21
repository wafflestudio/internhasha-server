package com.wafflestudio.internhasha.auth.dto

import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.auth.persistence.UserEntity

data class UserBrief(
    val id: String,
    val userRole: UserRole,
) {
    companion object {
        fun fromEntity(
            entity: UserEntity,
        ): UserBrief =
            UserBrief(
                id = entity.id,
                userRole = entity.userRole,
            )
    }
}
