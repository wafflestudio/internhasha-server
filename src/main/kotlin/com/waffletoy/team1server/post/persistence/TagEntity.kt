package com.waffletoy.team1server.post.persistence

import jakarta.persistence.*

@Entity
@Table(name = "tags")
open class TagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Int = 0,

    @Column(name = "tag", nullable = false, unique = true)
    val tag: String,

    @ManyToMany(mappedBy = "tags")
    val posts: MutableSet<PostEntity> = mutableSetOf()
)
