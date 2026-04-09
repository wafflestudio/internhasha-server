package com.wafflestudio.internhasha.coffeeChat.application.port

import com.wafflestudio.internhasha.coffeeChat.application.dto.CoffeeChatCreateCommand
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus

interface CoffeeChatPersistencePort {
    fun findCoffeeChatById(
        coffeeChatId: String,
        markAsRead: Boolean = false,
    ): CoffeeChat?

    fun findCoffeeChatByFilter(
        applicantId: String,
        positionId: String,
        status: CoffeeChatStatus,
    ): CoffeeChat?

    fun existsCoffeeChatByFilter(
        applicantId: String,
        positionId: String,
        status: CoffeeChatStatus,
    ): Boolean

    fun findAllCoffeeChatByApplicantId(applicantId: String): List<CoffeeChat>

    fun findAllByCompanyIdExceptCanceled(companyId: String): List<CoffeeChat>

    fun countCoffeeChatByApplicantIdAndChangedTrue(applicantId: String): Long

    fun countCoffeeChatByCompanyIdAndStatus(
        companyId: String,
        status: CoffeeChatStatus,
    ): Long

    fun createCoffeeChat(command: CoffeeChatCreateCommand): CoffeeChat

    fun updateCoffeeChatContent(
        coffeeChatId: String,
        content: String,
    ): CoffeeChat

    fun updateCoffeeChatStatus(
        coffeeChatId: String,
        status: CoffeeChatStatus,
    ): CoffeeChat
}
