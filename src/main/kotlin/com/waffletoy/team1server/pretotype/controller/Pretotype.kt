package com.waffletoy.team1server.pretotype.controller

import com.waffletoy.team1server.pretotype.persistence.PretotypeEntity
import java.time.Instant

class Pretotype (
    val email: String,
    val isSubscribed: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun fromEntity(entity: PretotypeEntity): Pretotype {
            return Pretotype(
                email = entity.email,
                isSubscribed = entity.isSubscribed,
                createdAt = entity.createdAt
                )
        }
    }
}