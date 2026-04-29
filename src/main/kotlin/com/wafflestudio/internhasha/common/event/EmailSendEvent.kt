package com.wafflestudio.internhasha.common.event

import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDto
import com.wafflestudio.internhasha.email.domain.EmailType

data class EmailSendEvent(
    val emailType: EmailType,
    val to: String,
    val subject: String,
    val text: String,
    val coffeeChat: CoffeeChatDto? = null,
    val waitingCoffeeChatCount: Long = 0,
)
