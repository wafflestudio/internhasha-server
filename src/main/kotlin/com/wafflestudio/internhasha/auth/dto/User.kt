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
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                userRole = entity.userRole,
                email = entity.email,
            )
    }
}
