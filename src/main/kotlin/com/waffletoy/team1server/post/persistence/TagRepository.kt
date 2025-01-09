package com.waffletoy.team1server.post.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TagRepository : JpaRepository<TagEntity, String>, JpaSpecificationExecutor<TagEntity> {
    fun findByTag(tag: String): TagEntity?
}
