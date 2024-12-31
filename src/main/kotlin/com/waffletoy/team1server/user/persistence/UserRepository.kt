package com.waffletoy.team1server.user.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findByEmail(email: String): UserEntity?

    fun findByLoginID(loginID: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    fun existsByLoginID(userId: String): Boolean

    // Refresh Token으로 사용자 검색
    fun findByRefreshToken(refreshToken: String): UserEntity?
}
