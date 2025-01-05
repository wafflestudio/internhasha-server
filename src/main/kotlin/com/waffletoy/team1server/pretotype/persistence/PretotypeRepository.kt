package com.waffletoy.team1server.pretotype.persistence

import org.springframework.data.jpa.repository.JpaRepository

// ID는 Long
interface PretotypeRepository : JpaRepository<PretotypeEntity, Long> {
    fun findByEmail(email: String): PretotypeEntity?
}
