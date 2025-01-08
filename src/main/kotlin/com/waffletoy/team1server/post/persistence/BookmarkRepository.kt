package com.waffletoy.team1server.post.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : JpaRepository<BookmarkEntity, String> {
    fun deleteByUserIdAndPostId(
        userId: String,
        postId: String,
    )

    fun existsByUserIdAndPostId(
        userId: String,
        postId: String,
    ): Boolean

    fun findAllByUserId(userId: String): List<BookmarkEntity>
}
