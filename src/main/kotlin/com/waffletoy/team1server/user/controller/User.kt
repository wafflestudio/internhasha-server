package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.user.AuthProvider
import com.waffletoy.team1server.user.UserStatus
import com.waffletoy.team1server.user.persistence.UserEntity

// admin 클래스는 User를 상속하는 방식 고려할 것(isAdmin = true)
data class User(
    val id: String,
    val snuMail: String,
    val username: String,
    val phoneNumber: String?,
    val isAdmin: Boolean = false,
) {
    companion object {
        fun fromEntity(entity: UserEntity): User =
            User(
                id = entity.id,
                snuMail = entity.snuMail,
                username = entity.username,
                phoneNumber = entity.phoneNumber,
            )
    }
}
