package com.wafflestudio.internhasha.coffeeChat.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CoffeeChatContent(
    @field:NotBlank(message = "Content cannot be blank.")
    @field:Size(min = 1, max = 10000, message = "Content must be at most 10,000 characters long")
    val content: String,
)
