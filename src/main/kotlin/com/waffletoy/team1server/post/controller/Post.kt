package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.persistence.PostEntity
import com.waffletoy.team1server.post.persistence.TagEntity
import java.time.Instant

data class Post(
    val id: Int?,
    val authorId: Int,
    val title: String,
    val content: String?,
    val duration: Int,
    val tags: List<String>,
    val link: String,
    val endTime: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun fromEntity(entity: PostEntity): Post =
            Post(
                id = entity.id,
                authorId = entity.author.id!!,
                title = entity.title,
                content = entity.content,
                duration = entity.duration,
                tags = entity.tags.map(TagEntity::name),
                link = entity.link,
                endTime = entity.endTime,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
    }
}
