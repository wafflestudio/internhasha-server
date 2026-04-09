package com.wafflestudio.internhasha.auth.application.port

import com.wafflestudio.internhasha.auth.domain.Tokens
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.domain.UserSimple

interface TokenProviderPort {
    fun generateTokens(user: UserSimple): Tokens

    fun getUserIdFromToken(token: String): String?

    fun getUserRoleFromToken(token: String): UserRole?
}
