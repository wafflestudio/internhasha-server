package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.account.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookmarkRepository : JpaRepository<BookmarkEntity, String> {
    fun deleteByUserAndRole(
        user: UserEntity?,
        role: RoleEntity?,
    )

    fun existsByUserAndRole(
        user: UserEntity?,
        role: RoleEntity?,
    ): Boolean

    fun findAllByUser(user: UserEntity): List<BookmarkEntity>
}
