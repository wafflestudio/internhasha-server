package com.waffletoy.team1server.post.dto

import jakarta.persistence.Embeddable

@Embeddable
data class TagVo(
    val tag: String,
)
