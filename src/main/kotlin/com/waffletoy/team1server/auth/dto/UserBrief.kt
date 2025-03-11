package com.waffletoy.team1server.auth.dto

import com.waffletoy.team1server.auth.UserRole
import com.waffletoy.team1server.auth.persistence.UserEntity

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
