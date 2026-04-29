package com.wafflestudio.internhasha.auth.service.dto

data class UserWithTokens(
    val user: UserSimple,
    val tokens: Tokens,
)
