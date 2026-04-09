package com.wafflestudio.internhasha.auth.domain

data class UserSimple(
    val id: String,
    val userRole: UserRole,
) {
    companion object {
        fun fromUser(
            user: User,
        ): UserSimple =
            UserSimple(
                id = user.id,
                userRole = user.userRole,
            )
    }
}
