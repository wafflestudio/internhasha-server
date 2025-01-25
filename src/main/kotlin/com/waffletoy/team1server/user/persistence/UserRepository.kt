package com.waffletoy.team1server.user.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findBySnuMail(snuMail: String): UserEntity?

    fun findByGoogleLoginId(googleLoginId: String): UserEntity?

    fun findByLocalLoginId(localLoginId: String): UserEntity?

    fun existsBySnuMail(snuMail: String): Boolean

    fun existsByLocalLoginId(localLoginId: String): Boolean

    fun existsByGoogleLoginId(googleLoginId: String): Boolean

    fun deleteUserEntityBySnuMail(snuMail: String)
}
