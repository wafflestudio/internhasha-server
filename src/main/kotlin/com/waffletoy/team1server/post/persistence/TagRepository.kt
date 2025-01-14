package com.waffletoy.team1server.post.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<TagEntity, String>, JpaSpecificationExecutor<TagEntity> {
    fun findByTag(tag: String): TagEntity?
}
