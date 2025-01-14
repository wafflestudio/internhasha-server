package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.persistence.RoleEntity

data class Role(
    val category: Category,
    val detail: String?,
    val headcount: String,
) {
    companion object {
        fun fromEntity(roleEntity: RoleEntity): Role {
            return Role(
                category = roleEntity.category,
                detail = roleEntity.detail,
                headcount = roleEntity.headcount,
            )
        }
    }
}
