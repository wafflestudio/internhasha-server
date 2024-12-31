package com.waffletoy.team1server.user.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface UserRepository : JpaRepository<UserEntity, Int> {

    fun findByEmail(email: String): UserEntity?

    fun findByLoginID(loginID: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    fun existsByLoginID(userId: String): Boolean

    // Refresh Token으로 사용자 검색
    fun findByRefreshToken(refreshToken: String): UserEntity?

    // 유효한 Refresh Token으로 사용자 검색
    fun findByRefreshTokenAndRefreshTokenExpiresAtAfter(
        refreshToken: String,
        now: Instant,
    ): UserEntity?
}
