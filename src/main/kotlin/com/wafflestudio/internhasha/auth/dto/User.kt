package com.wafflestudio.internhasha.auth.dto

import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.auth.persistence.UserEntity
import java.time.LocalDateTime

data class User(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val userRole: UserRole,
    val email: String?,
) {
    companion object {
        fun fromEntity(
            entity: UserEntity,
        ): User =
            User(
                id = requireNotNull(entity.id) { "UserEntity id must not be null" },
                name = entity.name,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                userRole = entity.getRole(),
                email = entity.email,
            )
    }
}
