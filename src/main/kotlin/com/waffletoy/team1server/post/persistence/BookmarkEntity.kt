package com.waffletoy.team1server.post.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "bookmarks")
open class BookmarkEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "USER_ID", nullable = false, unique = true)
    var userId: String,
    @Column(name = "POST_ID", nullable = false, unique = true)
    var postId: String,
)
