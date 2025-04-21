package com.waffletoy.internhasha.company.dto

import jakarta.persistence.Embeddable

@Embeddable
data class TagVo(
    val tag: String,
)
