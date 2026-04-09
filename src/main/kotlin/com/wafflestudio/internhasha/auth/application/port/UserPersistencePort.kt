package com.wafflestudio.internhasha.auth.application.port

import com.wafflestudio.internhasha.auth.domain.User

interface UserPersistencePort {
    fun findUserById(userId: String): User?

    fun findUserByEmail(email: String): User?

    fun existsUserByEmail(email: String): Boolean

    fun changePassword(
        userId: String,
        newPasswordHash: String,
    )

    fun saveUser(user: User): User

    fun deleteUser(userId: String)
}
