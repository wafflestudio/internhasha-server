package com.waffletoy.team1server.coffeeChat.persistence

import com.waffletoy.team1server.user.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CoffeeChatRepository : JpaRepository<CoffeeChatEntity, String> {
    fun findAllByUserId(userId: String): List<CoffeeChatEntity>
    fun deleteAllByUser(userEntity: UserEntity)
}
