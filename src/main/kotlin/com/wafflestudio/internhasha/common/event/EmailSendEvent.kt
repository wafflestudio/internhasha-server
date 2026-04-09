package com.wafflestudio.internhasha.common.event

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.email.EmailType

data class EmailSendEvent(
    val emailType: EmailType,
    val to: String,
    val subject: String,
    val text: String,
    val coffeeChat: CoffeeChat? = null,
    val waitingCoffeeChatCount: Long = 0,
)
