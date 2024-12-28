package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.participation.persistence.ParticipationEntity
import com.waffletoy.team1server.user.persistence.UserEntity
import jakarta.persistence.*
import java.time.Instant

@Entity(name = "posts")
@Table(name = "posts")
data class PostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    val author: UserEntity,
    @Column(name = "title", nullable = false, length = 225)
    val title: String,
    @Column(name = "content", columnDefinition = "TEXT")
    val content: String? = null,
    @Column(name = "duration", nullable = false)
    val duration: Int,
    @ManyToMany
    @JoinTable(
        name = "post_tags",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")],
    )
    val tags: Set<TagEntity> = emptySet(),
    @OneToMany(mappedBy = "post")
    val participations: Set<ParticipationEntity> = emptySet(),
    @Column(name = "link", nullable = false, length = 225)
    val link: String,
    @Column(name = "end_time", nullable = false)
    val endTime: Instant,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
)
