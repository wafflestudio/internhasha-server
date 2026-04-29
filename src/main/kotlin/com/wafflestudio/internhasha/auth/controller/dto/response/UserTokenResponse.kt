package com.wafflestudio.internhasha.auth.controller.dto.response

data class UserTokenResponse(
    val user: UserResponse,
    val token: String,
)
