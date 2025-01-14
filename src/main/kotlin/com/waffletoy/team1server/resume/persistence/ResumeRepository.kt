package com.waffletoy.team1server.resume.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResumeRepository : JpaRepository<ResumeEntity, String> {
    fun findByPostId(postId: String): List<ResumeEntity>

    fun findByUserId(userId: String): List<ResumeEntity>
}
