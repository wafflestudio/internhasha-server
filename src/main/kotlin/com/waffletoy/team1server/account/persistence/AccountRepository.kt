package com.waffletoy.team1server.account.persistence

import com.waffletoy.team1server.account.controller.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long> {
    fun findByLocalId(loginID: String): AdminEntity?

    fun existsByLocalId(userId: String): Boolean
}
