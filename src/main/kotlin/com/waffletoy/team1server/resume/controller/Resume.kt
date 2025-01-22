package com.waffletoy.team1server.resume.controller

import com.waffletoy.team1server.resume.persistence.ResumeEntity
import com.waffletoy.team1server.user.dtos.User
import java.time.LocalDateTime

data class Resume(
    val id: String,
    val postId: String?,
    val author: User,
    val content: String,
    val phoneNumber: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(resumeEntity: ResumeEntity) =
            Resume(
                id = resumeEntity.id,
                postId = resumeEntity.role?.id,
                author = User.fromEntity(resumeEntity.user),
                content = resumeEntity.content ?: "",
                createdAt = resumeEntity.createdAt,
                phoneNumber = resumeEntity.phoneNumber ?: "",
            )
    }
}
