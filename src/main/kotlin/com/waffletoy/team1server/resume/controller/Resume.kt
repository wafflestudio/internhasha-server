package com.waffletoy.team1server.resume.controller

import com.waffletoy.team1server.account.controller.User
import com.waffletoy.team1server.resume.persistence.ResumeEntity
import java.time.LocalDateTime

data class Resume(
    val id: String,
    val postId: String,
    val author: User,
    val content: String,
    val phoneNumber: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(resumeEntity: ResumeEntity) =
            Resume(
                id = resumeEntity.id,
                postId = resumeEntity.post.id,
                author =
                    User(
                        id = resumeEntity.user.id,
                        snuMail = resumeEntity.user.snuMail,
                        username = resumeEntity.user.username,
                        phoneNumber = resumeEntity.user.phoneNumber,
                        isAdmin = false,
                    ),
                content = resumeEntity.content ?: "",
                createdAt = resumeEntity.createdAt,
                phoneNumber = resumeEntity.phoneNumber ?: "",
            )
    }
}
