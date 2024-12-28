package com.waffletoy.team1server.post.persistence

import jakarta.persistence.*

@Entity(name = "tags")
@Table(name = "tags")
data class TagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    @ManyToMany(mappedBy = "tags")
    val posts: Set<PostEntity> = emptySet(),
)
