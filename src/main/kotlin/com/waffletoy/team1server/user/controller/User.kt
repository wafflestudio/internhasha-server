package com.waffletoy.team1server.user.controller

import com.waffletoy.team1server.post.controller.Post
import com.waffletoy.team1server.user.persistence.UserEntity

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val authoredPosts: Set<Post>,
) {
    companion object {
        fun fromEntity(entity: UserEntity): User =
            User(
                id = entity.id!!,
                name = entity.name,
                email = entity.email,
                phoneNumber = entity.phoneNumber,
                authoredPosts = entity.authoredPosts.map { Post.fromEntity(it) }.toSet(),
            )
    }
}
