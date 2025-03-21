package com.waffletoy.team1server.coffeeChat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.waffletoy.team1server.auth.UserRole
import com.waffletoy.team1server.auth.persistence.UserEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CoffeeChatUserInfo(
    val name: String,
    val imageKey: String?,
) {
    companion object {
        fun fromEntity(
            entity: UserEntity,
        ): CoffeeChatUserInfo {
            return when (entity.userRole) {
                UserRole.APPLICANT ->
                    CoffeeChatUserInfo(
                        entity.name,
                        entity.profileImageLink,
                        // entity.applicant?.imageLink,
                    )
                UserRole.COMPANY ->
                    CoffeeChatUserInfo(
                        entity.name,
                        entity.company?.profileImageKey,
                    )
            }
        }
    }
}
