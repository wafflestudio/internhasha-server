package com.waffletoy.team1server.user.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByEmail(email: String): UserEntity?

    fun findByLoginId(loginID: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    fun existsByLoginId(userId: String): Boolean
}
