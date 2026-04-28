package com.wafflestudio.internhasha.coffeeChat.service.dto

data class CoffeeChatDetailList(
    val succeeded: List<CoffeeChatDto>,
    val failed: List<CoffeeChatDto>,
)
