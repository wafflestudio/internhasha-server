package com.waffletoy.team1server.user.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findBySnuMail(snuMail: String): UserEntity?

    fun findByLocalLoginId(localLoginId: String): UserEntity?

    fun existsBySnuMail(snuMail: String): Boolean

    fun existsByLocalLoginId(localLoginId: String): Boolean

    fun deleteUserEntityById(id: String)
}
