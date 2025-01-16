package com.waffletoy.team1server.post.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "links")
class LinkEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "LINK", nullable = false)
    open var link: String,
    @Column(name = "DESCRIPTION")
    open var description: String? = null,
)
