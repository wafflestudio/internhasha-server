package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.post.controller.Post
import com.waffletoy.team1server.user.AuthProvider
import com.waffletoy.team1server.user.UserStatus
import com.waffletoy.team1server.user.persistence.UserEntity

data class User(
    val id: String,
    val email: String,
    val nickname: String,
    val status: UserStatus,
    val authProvider: AuthProvider,
    val loginID: String?,
    val refreshToken: String?,
    val authoredPosts: Set<Post>,
) {
    companion object {
        fun fromEntity(entity: UserEntity): User =
            User(
                id = entity.id,
                email = entity.email,
                nickname = entity.nickname,
                status = entity.status,
                authProvider = entity.authProvider,
                refreshToken = entity.refreshToken,
                loginID = entity.loginID,
                authoredPosts = entity.authoredPosts.map { Post.fromEntity(it) }.toSet(),
            )
    }
}
