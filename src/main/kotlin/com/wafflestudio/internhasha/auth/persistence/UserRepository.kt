package com.wafflestudio.internhasha.auth.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    fun deleteUserEntityById(id: String)
}
