package com.waffletoy.team1server.user

enum class UserStatus(val code: Int) {
    INACTIVE(0),
    ACTIVE(1),
}

enum class AuthProvider {
    LOCAL,
    GOOGLE,
}
