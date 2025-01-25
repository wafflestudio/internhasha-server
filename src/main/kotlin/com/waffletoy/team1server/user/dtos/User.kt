package com.waffletoy.team1server.user.dtos

import com.waffletoy.team1server.resume.controller.Resume
import com.waffletoy.team1server.user.UserRole
import com.waffletoy.team1server.user.persistence.UserEntity
import java.time.LocalDateTime

data class User(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val userRole: UserRole,
    val snuMail: String?,
    val phoneNumber: String?,
    val resumes: List<Resume>?,
//    val posts: List<Post>?,
    val profileImageLink: String?,
    val isMerged: Boolean,
) {
    companion object {
        fun fromEntity(
            entity: UserEntity,
            isMerged: Boolean = false,
        ): User =
            User(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                userRole = entity.userRole,
                snuMail = entity.snuMail,
                phoneNumber = entity.phoneNumber,
                resumes = entity.resumes.map { Resume.fromEntity(it) },
//                posts = entity.posts.map { Post.fromEntity(it.roles) },
                profileImageLink = entity.profileImageLink,
                isMerged = isMerged,
            )
    }
}
