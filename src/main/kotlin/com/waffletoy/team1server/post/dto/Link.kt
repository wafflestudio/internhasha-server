package com.waffletoy.team1server.post.dto

import jakarta.persistence.Embeddable

@Embeddable
data class Link(
    val description: String,
    val link: String,
)

// 표시할 텍스트가 없으면 링크 자체로 표시
