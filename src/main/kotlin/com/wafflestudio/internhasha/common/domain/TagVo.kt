package com.wafflestudio.internhasha.common.domain

import jakarta.persistence.Embeddable

@Embeddable
data class TagVo(
    val tag: String,
)
