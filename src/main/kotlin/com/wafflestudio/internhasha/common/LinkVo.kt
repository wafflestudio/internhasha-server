package com.wafflestudio.internhasha.common

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class LinkVo(
    val description: String,
    @Column(length = 2048)
    val link: String,
)
