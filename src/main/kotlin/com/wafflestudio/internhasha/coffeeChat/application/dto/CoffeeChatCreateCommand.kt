package com.wafflestudio.internhasha.coffeeChat.application.dto

data class CoffeeChatCreateCommand(
    val applicantId: String,
    val positionId: String,
    val content: String,
)
