package com.waffletoy.team1server.post.dto

data class AuthorBrief(
    val id: String,
    val name: String,
    val profileImageKey: String? = null,
)
