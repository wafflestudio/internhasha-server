package com.waffletoy.team1server.resume.controller

import com.waffletoy.team1server.resume.persistence.ResumeEntity
import com.waffletoy.team1server.user.dtos.User
import java.time.LocalDateTime

data class Resume(
    val id: String,
    val positionTitle: String? = null,
    val companyName: String? = null,
    val author: User,
    val content: String,
    val phoneNumber: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(
            resumeEntity: ResumeEntity,
//            includeAuthor: Boolean = true,
        ): Resume =
            Resume(
                id = resumeEntity.id,
                positionTitle = resumeEntity.position?.title,
                companyName = resumeEntity.position?.company?.companyName,
//                author = if (includeAuthor) User.fromEntity(resumeEntity.user, includeResumes = false) else null,
                author = User.fromEntity(resumeEntity.user),
                content = resumeEntity.content ?: "",
                createdAt = resumeEntity.createdAt,
                phoneNumber = resumeEntity.phoneNumber ?: "",
            )
    }
}
