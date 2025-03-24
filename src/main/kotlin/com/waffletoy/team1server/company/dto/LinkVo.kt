package com.waffletoy.team1server.company.dto

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class LinkVo(
    val description: String,
    @Column(length = 2048)
    val link: String,
)

// 표시할 텍스트가 없으면 링크 자체로 표시
