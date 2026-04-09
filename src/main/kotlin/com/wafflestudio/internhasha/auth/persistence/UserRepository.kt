package com.wafflestudio.internhasha.auth.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByIdAndDeletedAtIsNull(id: String): UserEntity?

    fun findByEmailAndDeletedAtIsNull(email: String): UserEntity?

    fun existsByEmailAndDeletedAtIsNull(email: String): Boolean

    fun deleteUserEntityById(id: String)
}
