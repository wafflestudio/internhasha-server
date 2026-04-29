package com.wafflestudio.internhasha.auth.repository

import com.wafflestudio.internhasha.auth.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findByIdAndDeletedAtIsNull(id: String): User?

    fun findByEmailAndDeletedAtIsNull(email: String): User?

    fun existsByEmailAndDeletedAtIsNull(email: String): Boolean
}
