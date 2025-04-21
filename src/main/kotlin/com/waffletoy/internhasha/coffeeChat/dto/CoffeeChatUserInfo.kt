package com.waffletoy.internhasha.coffeeChat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.waffletoy.internhasha.auth.UserRole
import com.waffletoy.internhasha.auth.persistence.UserEntity

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
                        entity.applicant?.profileImageKey,
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
