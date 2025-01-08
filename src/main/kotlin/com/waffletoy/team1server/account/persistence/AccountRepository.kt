package com.waffletoy.team1server.account.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, String> {
    fun findByLocalId(loginID: String): AccountEntity?

    fun existsByLocalId(userId: String): Boolean
}
