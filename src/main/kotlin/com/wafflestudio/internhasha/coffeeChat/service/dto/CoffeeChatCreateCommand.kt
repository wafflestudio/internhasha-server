package com.wafflestudio.internhasha.coffeeChat.service.dto

data class CoffeeChatCreateCommand(
    val applicantId: String,
    val positionId: String,
    val content: String,
)
