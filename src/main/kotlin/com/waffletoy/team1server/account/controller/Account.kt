package com.waffletoy.team1server.account.controller

import com.waffletoy.team1server.account.persistence.AccountEntity

data class Account(
    val id: String,
    val username: String,
) {
    companion object {
        fun fromEntity(entity: AccountEntity): Account =
            Account(
                id = entity.id,
                username = entity.username,
            )
    }
}
