package com.waffletoy.team1server.user.dtos

import com.waffletoy.team1server.user.UserRole
import com.waffletoy.team1server.user.persistence.UserEntity

data class UserBrief(
    val id: String,
    val name: String,
    val userRole: UserRole,
    val imageLink: String?,
) {
    companion object {
        fun fromEntity(
            entity: UserEntity,
        ): UserBrief =
            UserBrief(
                id = entity.id,
                name = entity.name,
                userRole = entity.userRole,
                imageLink = entity.profileImageLink,
            )
    }
}
