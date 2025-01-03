package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.user.AuthProvider
import com.waffletoy.team1server.user.UserStatus
import com.waffletoy.team1server.user.persistence.UserEntity

data class User(
    val id: String,
    val snuMail: String,
    val nickname: String,
    val status: UserStatus,
    val authProvider: AuthProvider,
    val loginId: String?,
    val googleId: String?,
    val googleEmail: String?,
) {
    companion object {
        fun fromEntity(entity: UserEntity): User =
            User(
                id = entity.id,
                snuMail = entity.snuMail,
                nickname = entity.nickname,
                status = entity.status,
                authProvider = entity.authProvider,
                loginId = entity.loginId,
                googleId = entity.googleId,
                googleEmail = entity.googleEmail,
            )
    }
}
