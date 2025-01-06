package com.waffletoy.team1server.account.controller

import com.waffletoy.team1server.account.persistence.AccountEntity
import com.waffletoy.team1server.account.persistence.AdminEntity

data class UserOrAdmin(
    val id: String,
    val username: String,
    val isAdmin: Boolean,
) {
    companion object {
        fun fromEntity(entity: AccountEntity): UserOrAdmin =
            UserOrAdmin(
                id = entity.id,
                username = entity.username,
                isAdmin = entity is AdminEntity,
            )
    }
}
