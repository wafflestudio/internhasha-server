package com.waffletoy.team1server.post.dto

import jakarta.persistence.Embeddable

@Embeddable
data class Tag(
    val tag: String,
)
