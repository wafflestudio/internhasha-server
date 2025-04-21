package com.waffletoy.internhasha.echoserver.service

import org.springframework.stereotype.Service

@Service
class EchoService {
    fun convertToUpperCase(input: String): String {
        return input.uppercase() + "!"
    }
}
