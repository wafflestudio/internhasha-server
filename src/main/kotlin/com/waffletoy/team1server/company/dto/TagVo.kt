package com.waffletoy.team1server.company.dto

import jakarta.persistence.Embeddable

@Embeddable
data class TagVo(
    val tag: String,
)
