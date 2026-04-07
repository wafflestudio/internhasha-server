package com.wafflestudio.internhasha.common

import jakarta.persistence.Embeddable

@Embeddable
data class TagVo(
    val tag: String,
)
