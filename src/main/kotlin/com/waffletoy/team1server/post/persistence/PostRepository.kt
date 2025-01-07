package com.waffletoy.team1server.post.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<PostEntity, String> {
}