package com.wafflestudio.internhasha.coffeeChat.service.dto

import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.company.domain.Company

data class CoffeeChatUserInfo(
    val id: String,
    val name: String,
    val email: String?,
    val profileImageKey: String?,
) {
    companion object {
        fun fromEntity(entity: User): CoffeeChatUserInfo {
            return when (entity) {
                is Applicant ->
                    CoffeeChatUserInfo(
                        id = requireNotNull(entity.id) { "Id must not be null" },
                        name = entity.name,
                        email = entity.email,
                        profileImageKey = entity.profileImageKey,
                    )

                is Company ->
                    CoffeeChatUserInfo(
                        id = requireNotNull(entity.id) { "Id must not be null" },
                        name = entity.name,
                        email = entity.email,
                        profileImageKey = entity.profileImageKey,
                    )

                else -> throw IllegalStateException("Unknown user type ${entity.javaClass.simpleName}")
            }
        }
    }
}
