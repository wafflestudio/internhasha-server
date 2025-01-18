package com.waffletoy.team1server.user.dtos

import com.waffletoy.team1server.post.persistence.CompanyEntity
import com.waffletoy.team1server.resume.persistence.ResumeEntity
import com.waffletoy.team1server.user.Role
import com.waffletoy.team1server.user.persistence.UserEntity
import java.time.LocalDateTime

data class User(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val role: Role,
    val snuMail: String?,
    val phoneNumber: String?,
    val resumes: List<ResumeEntity>?,
    val posts: List<CompanyEntity>?,
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
                role = entity.role,
                snuMail = entity.snuMail,
                phoneNumber = entity.phoneNumber,
                resumes = entity.resumes,
                posts = entity.posts,
                profileImageLink = entity.profileImageLink,
                isMerged = isMerged,
            )
    }
}
