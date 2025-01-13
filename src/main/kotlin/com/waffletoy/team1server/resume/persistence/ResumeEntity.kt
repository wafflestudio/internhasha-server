package com.waffletoy.team1server.resume.persistence

import com.waffletoy.team1server.account.persistence.UserEntity
import com.waffletoy.team1server.post.persistence.PostEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "resumes")
open class ResumeEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "CREATED_AT", nullable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "CONTENT", columnDefinition = "TEXT")
    val content: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    val post: PostEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    open val user: UserEntity,
)
