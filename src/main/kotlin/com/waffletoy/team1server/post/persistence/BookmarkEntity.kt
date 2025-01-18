package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.user.persistence.UserEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["USER_ID", "ROLE_ID"]),
    ],
)
class BookmarkEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    open val user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    open val role: RoleEntity,
)
