package com.wafflestudio.internhasha.post.controller.dto.response

data class AuthorBriefResponse(
    val id: String,
    val name: String,
    val profileImageKey: String? = null,
)
