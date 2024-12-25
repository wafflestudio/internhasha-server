package com.waffletoy.team1server.pretotype.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PretotypeRepository : JpaRepository<PretotypeEntity, String> {
    fun findByEmail(email: String): PretotypeEntity?
}
