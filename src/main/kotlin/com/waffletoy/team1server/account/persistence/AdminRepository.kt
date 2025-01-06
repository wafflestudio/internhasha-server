package com.waffletoy.team1server.account.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<AdminEntity, Long> {
    fun findByLocalId(loginID: String): AdminEntity?

    fun existsByLocalId(userId: String): Boolean
}
