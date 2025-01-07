package com.waffletoy.team1server.post.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "links")
open class LinkEntity (
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),

    // POST -> LINK 단방향 관계
    @Column(name = "LINK", nullable = false)
    open val link: String,
)