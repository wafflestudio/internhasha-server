package com.wafflestudio.internhasha.common.persistence

import jakarta.persistence.Embeddable

@Embeddable
data class TagVo(
    val tag: String,
)
