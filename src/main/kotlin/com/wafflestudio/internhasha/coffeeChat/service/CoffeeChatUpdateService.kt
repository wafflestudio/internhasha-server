package com.wafflestudio.internhasha.coffeeChat.service

import com.wafflestudio.internhasha.coffeeChat.persistence.CoffeeChatEntity
import com.wafflestudio.internhasha.coffeeChat.persistence.CoffeeChatRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CoffeeChatUpdateService(
    private val coffeeChatRepository: CoffeeChatRepository,
) {
    @Async
    @Transactional
    fun updateChangedFlagsAsync(changedList: List<CoffeeChatEntity>) {
        changedList.forEach {
            it.changed = false
            coffeeChatRepository.save(it)
        }
    }
}
