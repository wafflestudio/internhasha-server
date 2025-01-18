package com.waffletoy.team1server.resume.persistence

import com.waffletoy.team1server.post.persistence.RoleEntity
import com.waffletoy.team1server.user.persistence.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "resumes")
class ResumeEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "CONTENT", columnDefinition = "TEXT", nullable = true)
    open var content: String? = null,
    @Column(name = "PHONENUMBER", length = 20, nullable = true)
    open var phoneNumber: String? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    open val role: RoleEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    open val user: UserEntity,
) {
    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = createdAt
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
